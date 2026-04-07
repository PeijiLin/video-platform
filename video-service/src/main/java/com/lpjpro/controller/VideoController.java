package com.lpjpro.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.constant.BaseUserInfo;
import com.lpjpro.exception.BusinessException;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.model.ElasticIndex;
import com.lpjpro.model.video.DTO.PageVideoRequest;
import com.lpjpro.model.video.DTO.RecommVideoRequest;
import com.lpjpro.model.video.DTO.VideoSearchRequest;
import com.lpjpro.model.video.DTO.VideoUpdateRequest;
import com.lpjpro.model.video.VO.GetVideoVO;
import com.lpjpro.model.video.VO.VideoPageVO;
import com.lpjpro.model.video.entity.Video;
import com.lpjpro.service.VideoService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ResponseBody
@Controller
@RequestMapping("/video")
/**
 *  视频相关接口
 *  @auther hl
 */
public class VideoController {

    @Resource
    private VideoService videoService;


    /**
     * 根据类型分页查询视频列表
     * @param pageVideoRequest 类型名
     * @return 视频列表
     */
    @PostMapping("/public/list")
    public BaseResponse<IPage<VideoPageVO>> pageVideo(@RequestBody PageVideoRequest pageVideoRequest) {
        ThrowsUtils.throwIf(CommonHandle.isNull(pageVideoRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(videoService.pageVideo(pageVideoRequest));
    }

    /**
     * 根据权重条件和视频id进行查询
     * @param recommVideoRequest
     * @return
     */
    @PostMapping("/getList")
    public BaseResponse<List<List<Video>>> getList(@RequestBody RecommVideoRequest recommVideoRequest) {
        Map<String, Integer> videoSum = recommVideoRequest.getVideoSum();
        List<Long> videoIds = recommVideoRequest.getVideoIds();
        List<List<Video>> lists = new ArrayList<>();
        videoSum.forEach((key,count) -> {
            LambdaQueryWrapper<Video> videoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            videoLambdaQueryWrapper.eq(Video::getCategoryId, Long.valueOf(key)).orderByDesc(Video::getCreatedTime, Video::getViews, Video::getLikes)
                    .last("limit " + count);
            if (!videoIds.isEmpty()) {
                videoLambdaQueryWrapper.notIn(Video::getId, videoIds);
            }
            List<Video> video = videoService.list(videoLambdaQueryWrapper);
            lists.add(video);
        });
        return ResultUtils.success(lists);
    }


    /**
     * 根据id查询视频
     * @param id 视频id
     * @return 视频
     */
    @GetMapping("/public/get")
    public BaseResponse<GetVideoVO> getVideoById(@RequestParam(value = "id") Long id) {
        ThrowsUtils.throwIf(CommonHandle.isNull(id) || id <= 0, ErrorCode.PARAMS_ERROR);
        Video video = videoService.getById(id);

        ThrowsUtils.throwIf(CommonHandle.isNull(video),ErrorCode.NOT_FOUND_ERROR);
        GetVideoVO getVideoVO = new GetVideoVO();
        BeanUtil.copyProperties(video,getVideoVO);
        return ResultUtils.success(getVideoVO);
    }

    /**
     * 根据用户id查询创作的视频
     * @return 视频
     */
    @GetMapping("/get/userId")
    public BaseResponse<List<GetVideoVO>> getVideoByUserId() {
        Long currentUserId = BaseUserInfo.getCurrentUserId();
        LambdaQueryWrapper<Video> videoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        videoLambdaQueryWrapper.eq(Video::getUserId, currentUserId);
        List<Video> list = videoService.list(videoLambdaQueryWrapper);
        List<GetVideoVO> videoVOS = new ArrayList<>();
        for (Video video : list) {
            GetVideoVO getVideoVO = new GetVideoVO();
            BeanUtil.copyProperties(video,getVideoVO);
            videoVOS.add(getVideoVO);
        }
        return ResultUtils.success(videoVOS);
    }

    /**
     * 获取观看历史
     * @return
     */
    @GetMapping("/get/watchHistory")
    public BaseResponse<List<GetVideoVO>> getWatchHistory() {
        return ResultUtils.success(videoService.getWatchHistory());
    }

    /**
     * 根据关键词所有视频
     *
     * @param videoSearchRequest
     * @return
     */
    @PostMapping("/public/search")
    public BaseResponse<List<ElasticIndex>> searchVideo(VideoSearchRequest videoSearchRequest) throws IOException {
        ThrowsUtils.throwIf(CommonHandle.isNull(videoSearchRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(videoService.searchVideo(videoSearchRequest));
    }


    @PutMapping("/update/favorites")
    public BaseResponse updateVideoFavorites(Long id, Long count) {
        ThrowsUtils.throwIf(CommonHandle.isNull(id), ErrorCode.PARAMS_ERROR);
        LambdaUpdateWrapper<Video> videoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        videoLambdaUpdateWrapper.set(Video::getCollections, count).eq(Video::getId, id);
        boolean update = videoService.update(videoLambdaUpdateWrapper);
        ThrowsUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(null, "收藏成功");
    }

    @PutMapping("/update/likes")
    public BaseResponse updateVideoLikes(Long id, Long count) {
        ThrowsUtils.throwIf(CommonHandle.isNull(id), ErrorCode.PARAMS_ERROR);
        LambdaUpdateWrapper<Video> videoLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        videoLambdaUpdateWrapper.set(Video::getLikes, count).eq(Video::getId, id);
        boolean update = videoService.update(videoLambdaUpdateWrapper);
        ThrowsUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(null, "点赞成功");
    }

    @PostMapping("/update")
    public BaseResponse updateVideo(VideoUpdateRequest videoUpdateRequest) {
        ThrowsUtils.throwIf(CommonHandle.isNull(videoUpdateRequest), ErrorCode.PARAMS_ERROR);
        if (videoUpdateRequest.getVideoId() == null || videoUpdateRequest.getVideoId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        videoService.updateVideo(videoUpdateRequest);
        return ResultUtils.success(null, "更新成功");
    }

}
