package com.lpjpro.utils;

import com.lpjpro.exception.S3StorageException;
import com.lpjpro.model.s3.MultipartCompleteRequest;
import com.lpjpro.model.s3.MultipartInitResult;
import com.lpjpro.model.s3.PartInfo;
import com.lpjpro.model.s3.PartUploadTask;
import com.lpjpro.properties.VideoStorageProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * S3 视频存储服务类（优化版）
 * 支持：分片上传、预签名直传、秒传校验、续传查询
 *
 * @author HL
 */
@Service
public class S3VideoStorageService {

    private static final Logger log = LoggerFactory.getLogger(S3VideoStorageService.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final VideoStorageProperties properties;

    private ExecutorService multipartExecutor;

    public S3VideoStorageService(S3Client s3Client, S3Presigner s3Presigner, VideoStorageProperties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    // ========== 初始化/销毁 ==========

    @PostConstruct
    public void init() {
        var config = properties.getUpload().getMultipart();
        int poolSize = Optional.ofNullable(config.getThreadPoolSize()).orElse(4);
        int queueSize = Optional.ofNullable(config.getQueueCapacity()).orElse(1000);

        this.multipartExecutor = new ThreadPoolExecutor(
                poolSize,
                poolSize * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueSize),
                r -> new Thread(r, "s3-multipart-" + System.nanoTime()),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        log.info("S3 multipart executor initialized: core={}, max={}, queue={}",
                poolSize, poolSize * 2, queueSize);
    }

    @PreDestroy
    public void shutdown() {
        if (multipartExecutor != null && !multipartExecutor.isShutdown()) {
            multipartExecutor.shutdown();
            try {
                if (!multipartExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    log.warn("Force shutdown multipart executor");
                    multipartExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                multipartExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // ========== 工具方法 ==========

    private String getBucket(String type) {
        var bucket = properties.getBucket();
        return switch (type.toLowerCase()) {
            case "video" -> bucket.getVideo();
            case "cover" -> bucket.getCover();
            case "temp"  -> bucket.getTemp();
            case "avatar" -> bucket.getAvatar();
            default -> bucket.getVideo();
        };
    }

    private String cleanKey(String key) {
        return (key != null && key.startsWith("/")) ? key.substring(1) : key;
    }

    private String resolveDomain() {
        VideoStorageProperties.Cdn cdn = properties.getCdn();
        if (Boolean.TRUE.equals(cdn.getEnabled()) &&
                cdn.getDomain() != null && !cdn.getDomain().isBlank()) {
            return cdn.getDomain().replaceAll("/$", "");
        }
        return properties.getEndpoint().replaceAll("/$", "");
    }

    public String buildFileUrl(String bucket, String key) {
        return String.format("%s/%s/%s", resolveDomain(), bucket, cleanKey(key));
    }

    // ========== 秒传支持：按 Metadata 查询 ==========

    /**
     * 检查文件是否已存在（通过自定义 Metadata 存储 file_hash）
     * ⚠️ 注意：MinIO 支持用户自定义 metadata，但查询需要遍历，生产环境建议用业务表+Redis
     *
     * @param type 业务类型
     * @param fileHash 文件哈希
     * @param userId 用户 ID（用于隔离）
     * @return 已存在的对象键，不存在则返回 null
     */
    public Optional<String> checkFileExistsByHash(String type, String fileHash, Long userId) {
        String bucket = getBucket(type);

        try {
            // 方案：列出指定前缀的对象，过滤 metadata 中的 file_hash
            // ⚠️ 性能提示：ListObjectsV2 是 O(N) 操作，大数据量时务必用业务表+Redis
            String prefix = String.format("user/%d/", userId);

            ListObjectsV2Response response = s3Client.listObjectsV2(
                    ListObjectsV2Request.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .build());

            return response.contents().stream()
                    .map(s3Object -> {
                        try {
                            HeadObjectResponse meta = s3Client.headObject(
                                    HeadObjectRequest.builder()
                                            .bucket(bucket)
                                            .key(s3Object.key())
                                            .build());
                            // 从 metadata 读取 file_hash（MinIO 会自动转小写）
                            String storedHash = meta.metadata().get("file-hash");
                            return (fileHash.equalsIgnoreCase(storedHash)) ? s3Object.key() : null;
                        } catch (Exception e) {
                            log.warn("Check metadata failed for key: {}", s3Object.key(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findFirst();

        } catch (Exception e) {
            log.error("Check file exists by hash failed", e);
            throw new S3StorageException("Check exists failed: " + e.getMessage(), e);
        }
    }

    // ========== 分片上传初始化 ==========

    /**
     * 初始化分片上传
     *
     * @param type 业务类型
     * @param key 文件键值（建议格式：user/{userId}/{uuid}_{fileName}）
     * @param contentType 内容类型
     * @param fileHash 文件哈希（用于秒传/审计，存入 Metadata）
     * @param expireHours 会话有效期（小时）
     * @return 初始化结果
     */
    public MultipartInitResult initMultipart(String type, String key, String contentType,
                                             String fileHash, Long expireHours) {
        String bucket = getBucket(type);
        String cleanKey = cleanKey(key);
        long expireSeconds = Optional.ofNullable(expireHours).orElse(24L) * 3600;

        try {
            // 构建请求：添加自定义 metadata 用于秒传校验
            Map<String, String> metadata = new HashMap<>();
            if (fileHash != null && !fileHash.isBlank()) {
                metadata.put("file-hash", fileHash.toLowerCase()); // MinIO metadata key 自动转小写
            }

            CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(cleanKey)
                    .contentType(contentType)
                    .metadata(metadata)
                    .build();

            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(request);

            MultipartInitResult result = new MultipartInitResult();
            result.setBucket(bucket);
            result.setKey(cleanKey);
            result.setUploadId(response.uploadId());
            result.setExpireTime(System.currentTimeMillis() + expireSeconds * 1000);

            log.info("Init multipart: bucket={}, key={}, uploadId={}", bucket, cleanKey, response.uploadId());
            return result;

        } catch (S3Exception e) {
            log.error("Init multipart failed: bucket={}, key={}", bucket, cleanKey, e);
            throw new S3StorageException("Init multipart failed: " + e.getMessage(), e);
        }
    }

    // ========== 分片预签名 URL（前端直传核心） ==========

    /**
     * 生成分片上传预签名 URL
     * ⚠️ 关键：必须包含 partNumber 和 uploadId 作为查询参数
     *
     * @param type 业务类型
     * @param key 文件键值
     * @param uploadId 上传会话 ID
     * @param partNumber 分片序号（1-10000）
     * @param expireSeconds 过期时间（秒），默认 900（15 分钟）
     * @return 预签名 URL
     */
    public String generatePresignedPartUrl(String type, String key, String uploadId,
                                           int partNumber, Integer expireSeconds) {
        if (partNumber < 1 || partNumber > 10000) {
            throw new IllegalArgumentException("partNumber must be between 1 and 10000");
        }

        String bucket = getBucket(type);
        String cleanKey = cleanKey(key);
        int expire = Optional.ofNullable(expireSeconds)
                .orElse(properties.getPresigned().getUploadExpireSeconds() != null ?
                        properties.getPresigned().getUploadExpireSeconds() : 900);

        try {
            // ✅ AWS SDK v2 正确写法：通过 overrideConfiguration 添加查询参数
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expire))
                    .putObjectRequest(b -> b
                            .bucket(bucket)
                            .key(cleanKey)
                            .overrideConfiguration(c -> c
                                    // ✅ 关键：分片上传必须带这两个参数
                                    .putRawQueryParameter("partNumber", String.valueOf(partNumber))
                                    .putRawQueryParameter("uploadId", uploadId)))
                    .build();

            PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
            log.debug("Generated presigned part url: part={}, expire={}s", partNumber, expire);
            return presigned.url().toString();

        } catch (Exception e) {
            log.error("Generate presigned part url failed", e);
            throw new S3StorageException("Generate presigned part url failed: " + e.getMessage(), e);
        }
    }

    // ========== 查询已上传分片（续传支持） ==========

    /**
     * 查询已上传的分片列表（用于断点续传）
     *
     * @param bucket 存储桶
     * @param key 对象键
     * @param uploadId 上传会话 ID
     * @param maxParts 最大返回数量（默认 1000）
     * @return 已上传分片信息列表
     */
    public List<PartInfo> listUploadedParts(String bucket, String key, String uploadId, Integer maxParts) {
        try {
            List<PartInfo> result = new ArrayList<>();
            String nextMarker = null;
            int limit = Optional.ofNullable(maxParts).orElse(1000);

            do {
                ListPartsRequest.Builder builder = ListPartsRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .uploadId(uploadId)
                        .maxParts(limit);

                if (nextMarker != null) {
                    builder.partNumberMarker(Integer.valueOf(nextMarker));
                }

                ListPartsResponse response = s3Client.listParts(builder.build());

                for (Part part : response.parts()) {
                    PartInfo info = new PartInfo();
                    info.setPartNumber(part.partNumber());
                    info.setEtag(part.eTag());
                    result.add(info);
                }

                nextMarker = String.valueOf(response.isTruncated() ? response.nextPartNumberMarker() : null);

            } while (nextMarker != null && result.size() < limit);

            log.debug("Listed {} uploaded parts for uploadId={}", result.size(), uploadId);
            return result;

        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                // UploadId 不存在或已合并/取消
                log.warn("Upload session not found: {}", uploadId);
                return Collections.emptyList();
            }
            log.error("List uploaded parts failed", e);
            throw new S3StorageException("List parts failed: " + e.getMessage(), e);
        }
    }

    // ========== 异步分片上传（后端代理模式） ==========

    /**
     * 进度回调接口
     */
    @FunctionalInterface
    public interface ProgressCallback {
        void onProgress(int completedParts, int totalParts, double percent);
    }

    /**
     * 异步上传多个分片（后端代理模式，适合内网/小文件）
     * ⚠️ 注意：前端直传场景不需要调用此方法，前端直接用预签名 URL 上传
     *
     * @param bucket 存储桶
     * @param key 对象键
     * @param uploadId 上传会话 ID
     * @param tasks 分片任务列表
     * @param callback 进度回调（可选）
     * @return 上传成功的分片信息（按 partNumber 排序）
     */
    public List<PartInfo> uploadPartsAsync(String bucket, String key, String uploadId,
                                           List<PartUploadTask> tasks, ProgressCallback callback) {
        if (multipartExecutor == null || multipartExecutor.isShutdown()) {
            throw new S3StorageException("Multipart executor not available");
        }

        int total = tasks.size();
        AtomicInteger completed = new AtomicInteger(0);

        List<Future<PartInfo>> futures = tasks.stream()
                .map(task -> multipartExecutor.submit(() -> {
                    try {
                        String eTag = uploadPart(bucket, key, uploadId,
                                task.getPartNumber(), task.getInputStream(), task.getPartSize());

                        // 进度回调
                        int done = completed.incrementAndGet();
                        if (callback != null) {
                            callback.onProgress(done, total, done * 100.0 / total);
                        }

                        PartInfo info = new PartInfo();
                        info.setPartNumber(task.getPartNumber());
                        info.setEtag(eTag);
                        return info;

                    } catch (Exception e) {
                        log.error("Upload part {} failed", task.getPartNumber(), e);
                        throw new CompletionException(e);
                    }
                }))
                .toList();

        // 收集结果（带超时）
        List<PartInfo> results = new ArrayList<>(total);
        for (Future<PartInfo> future : futures) {
            try {
                results.add(future.get(5, TimeUnit.MINUTES));
            } catch (TimeoutException e) {
                throw new S3StorageException("Part upload timeout", e);
            } catch (Exception e) {
                throw new S3StorageException("Part upload failed: " + e.getMessage(), e);
            }
        }

        // 按 partNumber 排序（确保合并时顺序正确）
        return results.stream()
                .sorted(Comparator.comparingInt(PartInfo::getPartNumber))
                .collect(Collectors.toList());
    }

    /**
     * 上传单个分片（内部方法）
     */
    private String uploadPart(String bucket, String key, String uploadId,
                              int partNumber, InputStream input, long partSize) {
        try {
            UploadPartRequest request = UploadPartRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .build();

            UploadPartResponse response = s3Client.uploadPart(
                    request, RequestBody.fromInputStream(input, partSize));

            return response.eTag();

        } catch (Exception e) {
            log.error("Upload part {} failed", partNumber, e);
            throw new S3StorageException("Upload part failed: " + e.getMessage(), e);
        }
    }

    // ========== 合并/取消 ==========

    /**
     * 完成分片上传
     */
    public String completeMultipart(MultipartCompleteRequest request) {
        try {
            List<CompletedPart> completedParts = request.getParts().stream()
                    .map(p -> CompletedPart.builder()
                            .partNumber(p.getPartNumber())
                            .eTag(p.getEtag())  // ✅ ETag 必须带双引号，MinIO 会校验
                            .build())
                    .sorted(Comparator.comparingInt(CompletedPart::partNumber))
                    .collect(Collectors.toList());

            CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(request.getBucket())
                    .key(request.getKey())
                    .uploadId(request.getUploadId())
                    .multipartUpload(CompletedMultipartUpload.builder()
                            .parts(completedParts)
                            .build())
                    .build();

            System.out.println(completeRequest);

            CompleteMultipartUploadResponse response = s3Client.completeMultipartUpload(completeRequest);

            log.info("Multipart completed: {}/{}, versionId={}",
                    request.getBucket(), request.getKey(), response.versionId());

            return buildFileUrl(request.getBucket(), request.getKey());

        } catch (S3Exception e) {
            log.error("Complete multipart failed", e);
            // 合并失败建议自动清理碎片
            abortMultipart(request.getBucket(), request.getKey(), request.getUploadId());
            throw new S3StorageException("Complete failed: " + e.getMessage(), e);
        }
    }

    /**
     * 取消分片上传（清理碎片）
     */
    public void abortMultipart(String bucket, String key, String uploadId) {
        try {
            AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .build();

            s3Client.abortMultipartUpload(request);
            log.info("Multipart aborted: {}/{}, uploadId={}", bucket, key, uploadId);

        } catch (Exception e) {
            // 清理失败只记录日志，不抛异常避免影响主流程
            log.warn("Abort multipart failed (non-fatal)", e);
        }
    }

    // ========== 其他预签名 URL ==========

    public String generatePresignedUploadUrl(String type, String key, String contentType, Integer expireSeconds) {
        String bucket = getBucket(type);
        String cleanKey = cleanKey(key);
        int expire = Optional.ofNullable(expireSeconds)
                .orElse(properties.getPresigned().getUploadExpireSeconds() != null ?
                        properties.getPresigned().getUploadExpireSeconds() : 900);

        try {
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expire))
                    .putObjectRequest(b -> b
                            .bucket(bucket)
                            .key(cleanKey)
                            .contentType(contentType))
                    .build();

            return s3Presigner.presignPutObject(presignRequest).url().toString();

        } catch (Exception e) {
            log.error("Generate presigned upload url failed", e);
            throw new S3StorageException("Generate presigned upload url failed", e);
        }
    }

    public String generatePresignedDownloadUrl(String type, String key, Integer expireSeconds) {
        String bucket = getBucket(type);
        String cleanKey = cleanKey(key);
        int expire = Optional.ofNullable(expireSeconds)
                .orElse(properties.getPresigned().getPlayExpireSeconds() != null ?
                        properties.getPresigned().getPlayExpireSeconds() : 7200);

        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expire))
                    .getObjectRequest(b -> b
                            .bucket(bucket)
                            .key(cleanKey))
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();

        } catch (Exception e) {
            log.error("Generate presigned download url failed", e);
            throw new S3StorageException("Generate presigned download url failed", e);
        }
    }

    // ========== 简单文件上传 ==========

    /**
     * 简单文件上传（适用于小文件，一次性上传）
     *
     * @param type 业务类型 (video, cover, temp, avatar)
     * @param key 文件键值
     * @param inputStream 文件输入流
     * @param contentLength 文件大小
     * @param contentType 文件内容类型
     * @return 文件访问URL
     */
    public String uploadFile(String type, String key, InputStream inputStream,
                            Long contentLength, String contentType) {
        String bucket = getBucket(type);
        String cleanKey = cleanKey(key);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(cleanKey)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));

            log.info("Simple file upload completed: {}/{}", bucket, cleanKey);
            return buildFileUrl(bucket, cleanKey);

        } catch (Exception e) {
            log.error("Simple file upload failed: {}/{}", bucket, cleanKey, e);
            throw new S3StorageException("Upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * 简单文件上传（带元数据）
     *
     * @param type 业务类型
     * @param key 文件键值
     * @param inputStream 文件输入流
     * @param contentLength 文件大小
     * @param contentType 文件内容类型
     * @param metadata 自定义元数据
     * @return 文件访问URL
     */
    public String uploadFileWithMetadata(String type, String key, InputStream inputStream,
                                       Long contentLength, String contentType,
                                       Map<String, String> metadata) {
        String bucket = getBucket(type);
        String cleanKey = cleanKey(key);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(cleanKey)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .metadata(metadata)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));

            log.info("Simple file upload with metadata completed: {}/{}", bucket, cleanKey);
            return buildFileUrl(bucket, cleanKey);

        } catch (Exception e) {
            log.error("Simple file upload with metadata failed: {}/{}", bucket, cleanKey, e);
            throw new S3StorageException("Upload failed: " + e.getMessage(), e);
        }
    }


    // ========== 基础文件操作 ==========

    public InputStream download(String type, String key) {
        String bucket = getBucket(type);
        String cleanKey = cleanKey(key);

        try {
            return s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(cleanKey)
                    .build());
        } catch (Exception e) {
            log.error("Download failed: {}/{}", bucket, cleanKey, e);
            throw new S3StorageException("Download failed: " + e.getMessage(), e);
        }
    }

    public boolean exists(String type, String key) {
        String bucket = getBucket(type);
        String cleanKey = cleanKey(key);

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(cleanKey)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Check exists failed", e);
            throw new S3StorageException("Check exists failed", e);
        }
    }

    public void delete(String type, String key) {
        String bucket = getBucket(type);
        String cleanKey = cleanKey(key);

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(cleanKey)
                    .build());
            log.info("Deleted: {}/{}", bucket, cleanKey);
        } catch (Exception e) {
            log.error("Delete failed: {}/{}", bucket, cleanKey, e);
            throw new S3StorageException("Delete failed: " + e.getMessage(), e);
        }
    }

    public HeadObjectResponse getMetadata(String type, String key) {
        String bucket = getBucket(type);
        String cleanKey = cleanKey(key);

        try {
            return s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(cleanKey)
                    .build());
        } catch (Exception e) {
            log.error("Get metadata failed", e);
            throw new S3StorageException("Get metadata failed: " + e.getMessage(), e);
        }
    }
}