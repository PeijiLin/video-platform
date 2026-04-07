package com.lpjpro.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.gson.reflect.TypeToken;
import com.lpjpro.constant.BaseUserInfo;
import com.lpjpro.constant.RedisKeys;
import com.lpjpro.exception.BusinessException;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.mapper.UploadSessionMapper;
import com.lpjpro.mapper.VideoMapper;
import com.lpjpro.model.*;
import com.lpjpro.model.s3.MultipartCompleteRequest;
import com.lpjpro.model.s3.MultipartInitResult;
import com.lpjpro.model.video.entity.Video;
import com.lpjpro.properties.VideoStorageProperties;
import com.lpjpro.service.VideoUploadService;
import com.lpjpro.utils.JSONUtils;
import com.lpjpro.utils.S3VideoStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author HL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoUploadServiceImpl implements VideoUploadService {

    private final S3VideoStorageService s3VideoStorageService;

    private final VideoStorageProperties videoStorageProperties;

    private final RedisTemplate<String, Object> redisTemplate;

    private final RedissonClient redissonClient;

    private final VideoMapper videoMapper;

    private final UploadSessionMapper uploadSessionMapper;

    // 会话有效期（小时）
    private static final int SESSION_EXPIRE_HOURS = 24;
    // 预签名 URL 有效期（分钟）
    private static final int PRESIGNED_URL_EXPIRE_MINUTES = 15;

    @Override
    public InitMultipartResponse initMultipartUpload(InitMultipartRequest request) throws InterruptedException {
        // 先查询hash是否存在
        String fileHash = request.getFileHash();
        Long userId = BaseUserInfo.getCurrentUserId();
        // 分布式锁：防止同一用户同一文件并发初始化
        String lockKey = String.format(RedisKeys.LOCK_UPLOAD, fileHash, userId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试加锁：等待 5 秒，锁定 30 秒
            if (!lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                log.warn("Failed to acquire lock for fileHash={}, userId={}", fileHash, userId);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统繁忙，请稍后重试");
            }
            return doInitUpload(request, userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "初始化中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private InitMultipartResponse doInitUpload(InitMultipartRequest request, Long userId) {
        String fileHash = request.getFileHash();
        InitMultipartResponse response = new InitMultipartResponse();

        // 1. 查 Redis 秒传缓存
        Object value = redisTemplate.opsForValue().get(String.format(RedisKeys.VIDEO_HASH, fileHash));
        Long cachedVideoId = null;
        if (value instanceof Integer) {
            cachedVideoId = ((Integer) value).longValue();
            // 使用longValue
        } else if (value instanceof Long) {
            cachedVideoId = (Long) value;
            // 使用longValue
        }
        if (cachedVideoId != null) {
            Video video = videoMapper.selectById(cachedVideoId);
            if (video != null) {
                response.setInstantUpload(true);
                response.setFileUrl(video.getVideoUrl());
                response.setUploadId(video.getId().toString());
                return response;
            }
        }

        // 2. Redis 未命中，查数据库 (Fallback)
        Video video = videoMapper.selectOne(new LambdaQueryWrapper<Video>().eq(Video::getFileHash, fileHash));
        // 秒传
        if (video != null) {
            InitMultipartResponse initMultipartResponse = new InitMultipartResponse();
            initMultipartResponse.setInstantUpload(true);
            initMultipartResponse.setFileUrl(video.getVideoUrl());
            initMultipartResponse.setUploadId(video.getId().toString());
            // 写回缓存
            redisTemplate.opsForValue().set(String.format(RedisKeys.VIDEO_HASH, fileHash), video.getId(), RedisKeys.SESSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
            return initMultipartResponse;
        }

        // 3. 查 Redis 续传索引
        String indexKey = String.format(RedisKeys.UPLOAD_HASH_INDEX, fileHash, userId);
        String uploadId = (String) redisTemplate.opsForValue().get(indexKey);

        if (uploadId != null) {
            // 验证会话详情是否存在
            String sessionKey = String.format(RedisKeys.UPLOAD_SESSION, uploadId);
            Map<Object, Object> sessionMap = redisTemplate.opsForHash().entries(sessionKey);

            if (!sessionMap.isEmpty() && "ACTIVE".equals(sessionMap.get("status"))) {
                log.info("Redis 命中续传，uploadId={}", uploadId);
                // 刷新过期时间
                redisTemplate.expire(sessionKey, RedisKeys.SESSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
                redisTemplate.expire(String.format(RedisKeys.UPLOAD_PARTS, uploadId), RedisKeys.SESSION_EXPIRE_SECONDS, TimeUnit.SECONDS);

                response.setInstantUpload(false);
                response.setUploadId(uploadId);
                response.setObjectKey((String) sessionMap.get("objectKey"));
                // 获取已上传分片
                response.setUploadedParts(getUploadedPartsFromRedis(uploadId));
                response.setExpireTime(System.currentTimeMillis() + RedisKeys.SESSION_EXPIRE_SECONDS * 1000);
                return response;
            }
        }


        // 4. 缓存不存在 查询会话
        UploadSession uploadSession = uploadSessionMapper.selectByHashAndUser(fileHash, BaseUserInfo.getCurrentUserId(), LocalDateTime.now());

        if (uploadSession != null) {
            Type type = new TypeToken<List<UploadPartInfo>>() {
            }.getType();
            List<UploadPartInfo> uploadPartInfos = JSONUtils.fromJson(uploadSession.getUploadedParts().toString(), type);
            InitMultipartResponse build = InitMultipartResponse.builder()
                    .instantUpload(false)
                    .objectKey(uploadSession.getObjectKey())
                    .uploadId(uploadSession.getUploadId())
                    .uploadedParts(uploadPartInfos)
                    .expireTime(dateToLocalDateTime(uploadSession.getExpireTime()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).build();
            // 写回缓存
            saveSessionToRedis(uploadSession.getUploadId(), uploadSession);
            return build;
        }

        // 4. 新建会话 (MinIO + DB + Redis)
        try {
            String fileName = request.getFileName();
            String objectKey = generateObjectKey(userId, request.getFileName());
            String fileExt = getFileExtension(fileName);
            MultipartInitResult s3Result = s3VideoStorageService.initMultipart(
                    videoStorageProperties.getBucket().getVideo(),           // 业务类型
                    objectKey,         // 对象键
                    request.getContentType(),       // 内容类型
                    fileHash,          // 文件哈希（用于秒传/审计）
                    (long) SESSION_EXPIRE_HOURS             // 会话有效期（小时）
            );

            // 4.1 写 DB
            UploadSession session = new UploadSession();
            session.setUploadId(s3Result.getUploadId());
            session.setObjectKey(s3Result.getKey());
            session.setBucket(s3Result.getBucket());
            session.setFileName(request.getFileName());
            session.setFileExt(fileExt);
            session.setFileHash(fileHash);
            session.setFileSize(request.getFileSize());
            session.setTotalParts(request.getTotalChunks());
            session.setUserId(userId);
            session.setUploadedParts("[]");  // 初始为空数组
            session.setStatus("ACTIVE");
            session.setExpireTime(localDateTimeToDate(LocalDateTime.now().plusHours(24)));
            session.setCreatedTime(localDateTimeToDate(LocalDateTime.now()));

            uploadSessionMapper.insert(session);
            String newUploadId = s3Result.getUploadId();
            // 4.2 写 Redis (双写)
            saveSessionToRedis(newUploadId, session);

            response.setInstantUpload(false);
            response.setUploadId(newUploadId);
            response.setObjectKey(objectKey);
            response.setUploadedParts(new ArrayList<>());
            response.setExpireTime(s3Result.getExpireTime());

            return response;

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int idx = fileName.lastIndexOf('.');
        return (idx > 0 && idx < fileName.length() - 1)
                ? fileName.substring(idx + 1).toLowerCase()
                : "";
    }

    /**
     * 生成对象存储路径
     */
    private String generateObjectKey(Long userId, String fileName) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE); // 20240326
        String safeFileName = fileName.replaceAll("[/\\\\?%*:|\"<>]", "_"); // 清理非法字符
        return String.format("raw/user/%d/%s/%s_%s", userId, date, uuid, safeFileName);
    }

    @Override
    public void saveUploadSession(String uploadId, String objectKey, InitMultipartRequest request, Long userId) {

    }

    @Override
    public PartSignatureResponse getPartSignature(PartSignatureRequest request) {
        String uploadId = request.getUploadId();
        String objectKey = request.getObjectKey();
        Integer partNumber = request.getPartNumber();
        if (StringUtils.isAnyBlank(uploadId, objectKey) || partNumber == null || partNumber <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String url = s3VideoStorageService.generatePresignedPartUrl(videoStorageProperties.getBucket().getVideo(), objectKey, uploadId, partNumber, null);
        url = url.replace(":9000", ":9002");
        return PartSignatureResponse.builder()
                .uploadUrl(url)
                .partNumber(partNumber)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CompleteMultipartResponse completeMultipartUpload(CompleteMultipartRequest request, Long userId) {
        boolean exists = s3VideoStorageService.exists(videoStorageProperties.getBucket().getVideo(), request.getObjectKey());
        String url = null;
        if (!exists) {
            MultipartCompleteRequest multipartCompleteRequest = new MultipartCompleteRequest();
            multipartCompleteRequest.setUploadId(request.getUploadId());
            multipartCompleteRequest.setKey(request.getObjectKey());
            multipartCompleteRequest.setBucket(videoStorageProperties.getBucket().getVideo());
            multipartCompleteRequest.setParts(request.getParts());
            url = s3VideoStorageService.completeMultipart(multipartCompleteRequest);
        } else {
            url = s3VideoStorageService.buildFileUrl(videoStorageProperties.getBucket().getVideo(), request.getObjectKey());
        }
        Video video = new Video();
        video.setVideoUrl(url);
        video.setUserId(userId);
        video.setRawObjectKey(request.getObjectKey());
        UploadSession uploadSession = uploadSessionMapper.selectByUploadIdAndUser(request.getUploadId(), userId);
        if (uploadSession != null) {
            String fileHash = uploadSession.getFileHash();
            video.setFileHash(fileHash);
            // 清楚会话缓存
            String indexKey = String.format(RedisKeys.UPLOAD_HASH_INDEX, fileHash, userId);
            redisTemplate.delete(indexKey);
            String format = String.format(RedisKeys.UPLOAD_SESSION, request.getUploadId());
            redisTemplate.delete(format);
        }
        int insert = videoMapper.insert(video);
        // 3. 查 Redis 续传索引

        // 更新会话状态
        uploadSessionMapper.update(new LambdaUpdateWrapper<UploadSession>()
                .set(UploadSession::getStatus, "COMPLETED").set(UploadSession::getUploadedParts, JSONUtils.toJson(request.getParts()))
                .set(UploadSession::getVideoId, video.getId())
                .eq(UploadSession::getUploadId, request.getUploadId()));

        CompleteMultipartResponse completeMultipartResponse = new CompleteMultipartResponse();
        completeMultipartResponse.setVideoId(video.getId());
        completeMultipartResponse.setVideoUrl(url);
        return completeMultipartResponse;
    }

    @Override
    public Video createVideoRecord(String objectKey, UploadSession session, Long userId) {
        return null;
    }

    @Override
    public SubmitVideoInfoResponse submitVideoInfo(SubmitVideoInfoRequest request, Long userId) {
        return null;
    }

    private List<UploadPartInfo> getUploadedPartsFromRedis(String uploadId) {
        String partsKey = String.format(RedisKeys.UPLOAD_PARTS, uploadId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(partsKey);
        List<UploadPartInfo> list = new ArrayList<>();
        entries.forEach((k, v) -> {
            UploadPartInfo info = new UploadPartInfo();
            info.setPartNumber(Integer.parseInt((String) k));
            info.setEtag((String) v);
            list.add(info);
        });
        return list;
    }

    private void refreshSessionExpireTime(UploadSession session) {
        session.setExpireTime(localDateTimeToDate(LocalDateTime.now().plusHours(SESSION_EXPIRE_HOURS)));
        session.setUpdatedTime(localDateTimeToDate(LocalDateTime.now()));
        uploadSessionMapper.updateById(session);
    }

    private void saveSessionToRedis(String uploadId, UploadSession session) {
        String sessionKey = String.format(RedisKeys.UPLOAD_SESSION, uploadId);
        Map<String, Object> map = new HashMap<>();
        map.put("uploadId", session.getUploadId());
        map.put("objectKey", session.getObjectKey());
        map.put("userId", session.getUserId().toString());
        map.put("status", "ACTIVE");
        map.put("fileHash", session.getFileHash());

        redisTemplate.opsForHash().putAll(sessionKey, map);
        redisTemplate.expire(sessionKey, RedisKeys.SESSION_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 建立 Hash -> UploadId 索引
        String indexKey = String.format(RedisKeys.UPLOAD_HASH_INDEX, session.getFileHash(), session.getUserId());
        redisTemplate.opsForValue().set(indexKey, uploadId, RedisKeys.SESSION_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    private Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

}
