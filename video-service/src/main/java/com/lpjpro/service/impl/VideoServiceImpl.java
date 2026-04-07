package com.lpjpro.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.command.CommandSettings;
import com.clickhouse.client.api.data_formats.ClickHouseBinaryFormatReader;
import com.clickhouse.client.api.query.QueryResponse;
import com.lpjpro.api.interaction.InteractionApi;
import com.lpjpro.api.user.UserApi;
import com.lpjpro.constant.BaseUserInfo;
import com.lpjpro.constant.RedisConstant;
import com.lpjpro.constant.StatusEnum;
import com.lpjpro.exception.BusinessException;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.mapper.VideoMapper;
import com.lpjpro.model.ElasticIndex;
import com.lpjpro.model.category.entity.Category;
import com.lpjpro.model.user.VO.UserVO;
import com.lpjpro.model.video.DTO.PageVideoRequest;
import com.lpjpro.model.video.DTO.VideoSearchRequest;
import com.lpjpro.model.video.DTO.VideoUpdateRequest;
import com.lpjpro.model.video.DTO.VideoUploadRequest;
import com.lpjpro.model.video.VO.GetVideoVO;
import com.lpjpro.model.video.VO.VideoPageVO;
import com.lpjpro.model.video.entity.Video;
import com.lpjpro.properties.VideoStorageProperties;
import com.lpjpro.service.CategoryService;
import com.lpjpro.service.VideoService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.JSONUtils;
import com.lpjpro.utils.S3VideoStorageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.lpjpro.constant.RedisConstant.VIDEO_CATEGORY;

/**
 * @author HL
 * @description 针对表【video(视频信息表)】的数据库操作Service实现
 * @createDate 2025-03-13 16:58:49
 */
@Slf4j
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video>
        implements VideoService {

    @Resource
    private CategoryService categoryService;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private S3VideoStorageService s3VideoStorageService;

    @Resource
    private VideoStorageProperties videoStorageProperties;

    @Resource
    private Client client;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserApi userApi;

    @Resource
    private InteractionApi interactionApi;

    @Resource
    private RestHighLevelClient restHighLevelClient;


    /**
     * 根据类型分页查询视频列表
     *
     * @param pageVideoRequest 类型名
     * @return 视频列表
     */
    @Override
    public IPage<VideoPageVO> pageVideo(PageVideoRequest pageVideoRequest) {
        Long currentUserId = BaseUserInfo.getCurrentUserId();
        String redisKey;
        if (currentUserId != null) {
            redisKey = String.format("%s:%s:%d-%d:user:%d", VIDEO_CATEGORY, pageVideoRequest.getCategoryName(), pageVideoRequest.getPageNum(), pageVideoRequest.getPageSize(), currentUserId);
        } else {
            redisKey = String.format("%s:%s:%d-%d", VIDEO_CATEGORY, pageVideoRequest.getCategoryName(), pageVideoRequest.getPageNum(), pageVideoRequest.getPageSize());
        }

        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Object cached = valueOperations.get(redisKey);

        if (cached != null) {
            if (cached instanceof IPage) {
                return (IPage<VideoPageVO>) cached;
            } else {
                return (Page<VideoPageVO>) cached;
            }
        }

        String categoryName = pageVideoRequest.getCategoryName();
        if (StringUtils.isBlank(categoryName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        IPage<VideoPageVO> page = new Page<>(pageVideoRequest.getPageNum(), pageVideoRequest.getPageSize());
        Long id;
        if (categoryName.equals(RedisConstant.CATEGORY_ALL)) {
            id = null;
        } else {
            LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
            categoryLambdaQueryWrapper.eq(Category::getName, categoryName);
            Category category = categoryService.getOne(categoryLambdaQueryWrapper);
            ThrowsUtils.throwIf(CommonHandle.isNull(category), ErrorCode.FILE_NOT_FOUND);
            id = category.getId();
        }
        int key = StatusEnum.PASSED.getKey();
        IPage<VideoPageVO> videoPageVOIPage = videoMapper.selectVideoVO(page, id, key);
        List<VideoPageVO> records = videoPageVOIPage.getRecords();
        List<Long> userIds = records.stream().map(VideoPageVO::getUserId).toList();
        List<UserVO> userVOS = userApi.getUserByIds(userIds).getData();
        for (int i = 0; i < records.size(); i++) {
            records.get(i).setUsername(userVOS.get(i).getUsername());
        }
        List<Long> videoIds = records.stream().map(VideoPageVO::getId).toList();

        if (currentUserId == null) {
            for (VideoPageVO record : records) {
                record.setIsLiked(false);
                record.setIsFavorited(false);
            }
            valueOperations.set(redisKey, page, 1, TimeUnit.HOURS);
            return page;
        }

        List<Boolean> isFavorites = interactionApi.batchFavorites(videoIds, currentUserId).getData();
        SetOperations<String, Object> stringObjectSetOperations = redisTemplate.opsForSet();
        Set<Object> members = stringObjectSetOperations.members(RedisConstant.USER_LIKED + currentUserId);
        List<Long> list = videoIds;
        if (members != null && !members.isEmpty()) {
            Set<Long> videoLikedIds = new HashSet<>();
            members.forEach(value -> videoLikedIds.add((Long) value));
            list = videoIds.stream().filter(aLong -> !videoLikedIds.contains(aLong)).toList();
            int count = 0;
            for (VideoPageVO record : records) {
                record.setIsFavorited(isFavorites.get(count++));
                if (videoLikedIds.contains(record.getId())) {
                    record.setIsLiked(true);
                }
            }
        }

        List<Boolean> isLikes = interactionApi.batchLikes(list, currentUserId).getData();
        List<Long> filteredList = IntStream.range(0, list.size())
                .filter(i -> Boolean.TRUE.equals(isLikes.get(i)))
                .mapToObj(list::get)
                .toList();
        list = new ArrayList<>(filteredList);

        for (VideoPageVO record : records) {
            if (list.contains(record.getId())) {
                record.setIsLiked(true);
            }
        }

        valueOperations.set(redisKey, page, 1, TimeUnit.HOURS);
        return page;
    }

    /**
     * 用户上传视频
     *
     * @param videoUploadRequest 视频信息
     * @return 视频id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long uploadVideo(VideoUploadRequest videoUploadRequest) throws IOException {
        authUploudParams(videoUploadRequest);
        Video video = new Video();
        BeanUtil.copyProperties(videoUploadRequest, video);
        Long currentUserId = BaseUserInfo.getCurrentUserId();
        ThrowsUtils.throwIf(CommonHandle.isNull(currentUserId), ErrorCode.SYSTEM_ERROR);
        video.setUserId(currentUserId);
        // 上传至对象存储空间
        MultipartFile videoFile = videoUploadRequest.getVideoFile();
        MultipartFile coverFile = videoUploadRequest.getCoverFile();
        String videoUrl = s3VideoStorageService.uploadFile(videoStorageProperties.getBucket().getVideo(), generateObjectKey(currentUserId, videoFile.getOriginalFilename()), videoFile.getInputStream(),videoFile.getSize(), "video/mp4");
        String coverUrl = s3VideoStorageService.uploadFile(videoStorageProperties.getBucket().getCover(), generateObjectKey(currentUserId, coverFile.getOriginalFilename()), coverFile.getInputStream(),coverFile.getSize(), "");
        video.setVideoUrl(videoUrl);
        video.setCoverUrl(coverUrl);
        // 填入数据库
        boolean save = this.save(video);
        ThrowsUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR);
        return video.getId();
    }

    /**
     * 生成对象存储路径
     */
    private String generateObjectKey(Long userId, String fileName) {
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // 20240326
        String safeFileName = fileName.replaceAll("[/\\\\?%*:|\"<>]", "_"); // 清理非法字符
        return String.format("raw/user/%d/%s/%s_%s", userId, date, uuid, safeFileName);
    }

    private void authUploudParams(VideoUploadRequest videoUploadRequest) {
        String title = videoUploadRequest.getTitle();
        String description = videoUploadRequest.getDescription();
        Long categoryId = videoUploadRequest.getCategoryId();
        String tags = videoUploadRequest.getTags();
        MultipartFile videoFile = videoUploadRequest.getVideoFile();
        MultipartFile coverFile = videoUploadRequest.getCoverFile();

        // TODO 优化，将if改成注解，减少业务代码量
        if (coverFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "视频文件不能为空");
        }

        if (videoFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "视频文件不能为空");
        }

        if (StringUtils.isAnyBlank(title, description, tags)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 标题校验，长度1-10个字符
        if (StringUtils.length(title) < 1 || StringUtils.length(title) > 10) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题长度需在1-10个字符之间");
        }

        // 描述校验，长度1-50个字符
        if (StringUtils.length(description) < 1 || StringUtils.length(description) > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述长度需在1-50个字符之间");
        }

        // 查询改分类id是否存在
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "输入参数有误，请求的分类不存在");
        }

        // 判断文件是否合法
        String videoFileName = videoFile.getOriginalFilename();
        String coverFileName = coverFile.getOriginalFilename();
        if (videoFileName == null || !StringUtils.containsAny(videoFileName, ".mp4", ".flv", ".f4v", ".webm", ".m4v", ".mov", ".3gp", ".avi")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "视频文件不能为空或者格式有误");
        }

        if (coverFileName == null || !StringUtils.containsAny(coverFileName, ".jpg", ".png", ".gif", ".webp", ".fpx", ".bmp", ".tif")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "封面图片为空或者格式有误");
        }
    }

    /**
     * 获取观看历史
     *
     * @return
     */
    @Override
    public List<GetVideoVO> getWatchHistory() {
        Long currentUserId = BaseUserInfo.getCurrentUserId();
        ThrowsUtils.throwIf(CommonHandle.isNull(currentUserId), ErrorCode.NOT_LOGIN_ERROR);

        String sql = """
                select videoId from user_behavior where userId = {userId:UInt64}
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("userId", currentUserId);
        List<Long> videoIds = new ArrayList<>();
        try (QueryResponse response = client.query(sql, params, new CommandSettings()).get()) {
            ClickHouseBinaryFormatReader reader = client.newBinaryFormatReader(response);
            while (reader.hasNext()) {
                reader.next();
                long videoId = reader.getLong("videoId");
                videoIds.add(videoId);
            }
        } catch (Exception e) {
            log.error("Failed to query watch history from ClickHouse", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取观看历史失败");
        }
        LambdaQueryWrapper<Video> videoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        videoLambdaQueryWrapper.orderByDesc(Video::getReviewedTime);
        if (!videoIds.isEmpty()) {
            videoLambdaQueryWrapper.in(Video::getId, videoIds);
        }
        List<Video> videos = this.list(videoLambdaQueryWrapper);
        List<GetVideoVO> videoVOS = new ArrayList<>();
        for (Video video : videos) {
            GetVideoVO videoVO = new GetVideoVO();
            BeanUtil.copyProperties(video, videoVO);
            videoVOS.add(videoVO);
        }
        return videoVOS;
    }


    /**
     * 根据关键词所有视频
     *
     * @param videoSearchRequest
     * @return
     */
    @Override
    public List<ElasticIndex> searchVideo(VideoSearchRequest videoSearchRequest) throws IOException {
        SearchRequest searchRequest = new SearchRequest("videos");
        searchRequest.source().query(QueryBuilders.multiMatchQuery(videoSearchRequest.getKeyword(), "title", "description", "tags"));
        searchRequest.source().from(videoSearchRequest.getPageNum()).size(videoSearchRequest.getPageSize());
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();
        List<ElasticIndex> elasticIndices = new ArrayList<>();
        for (SearchHit documentFields : hits1) {
            String sourceAsString = documentFields.getSourceAsString();
            ElasticIndex elasticIndex = JSONUtils.fromJson(sourceAsString, ElasticIndex.class);
            // 获取高亮结果
            Map<String, HighlightField> hfs = documentFields.getHighlightFields();
            if (!hfs.isEmpty()) {
                // 有高亮结果，获取name的高亮结果
                HighlightField hf = hfs.get("title");
                if (hf != null) {
                    // 获取第一个高亮结果片段，就是商品名称的高亮值
                    String hfTitle = hf.getFragments()[0].string();
                    elasticIndex.setTitle(hfTitle);
                }
            }
            elasticIndices.add(elasticIndex);
        }
        return elasticIndices;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateVideo(VideoUpdateRequest videoUpdateRequest) {
        MultipartFile coverFile = videoUpdateRequest.getCoverFile();
        Video video = BeanUtil.copyProperties(videoUpdateRequest, Video.class);
        if (coverFile != null) {
            Long currentUserId = BaseUserInfo.getCurrentUserId();
            String objectKey = getUniqueFileName(coverFile, currentUserId);
            try {
                String s = s3VideoStorageService.uploadFile(videoStorageProperties.getBucket().getVideo(), objectKey, coverFile.getInputStream(), coverFile.getSize(), "cover/png");
                video.setCoverUrl(s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        video.setId(videoUpdateRequest.getVideoId());
        boolean b = this.updateById(video);
        ThrowsUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR);
    }

    private String getUniqueFileName(MultipartFile multipartFile, Long userId) {
        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String format = "%s/%s";
        // 为了避免同名覆盖问题,构建新的文件名
        return String.format(format, userId, UUID.randomUUID() + suffix);
    }

}




