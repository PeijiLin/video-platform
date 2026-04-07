package com.lpjpro.utils;

import com.lpjpro.properties.S3Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner, S3Properties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    /**
     * 上传文件（InputStream）
     */
    public String uploadFile(InputStream inputStream, String fileName, String contentType) {
        return uploadFile(inputStream, fileName, contentType, properties.getBucket().getDefaultBucket());
    }

    public String uploadFile(InputStream inputStream, String fileName, String contentType, String bucketName) {
        validateFileType(contentType);
        String objectKey = generateObjectKey(fileName);

        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .build(),
                RequestBody.fromInputStream(inputStream, inputStream.available()));
            log.info("文件上传成功：{}/{}", bucketName, objectKey);
            return objectKey;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new StorageException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传文件（MultipartFile）
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, properties.getBucket().getDefaultBucket());
    }

    public String uploadFile(MultipartFile file, String bucketName) {
        validateFileSize(file.getSize());
        validateFileType(file.getContentType());
        String objectKey = generateObjectKey(file.getOriginalFilename());

        try {
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("文件上传成功：{}/{}", bucketName, objectKey);
            return objectKey;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new StorageException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String objectKey) {
        return downloadFile(objectKey, properties.getBucket().getDefaultBucket());
    }

    public InputStream downloadFile(String objectKey, String bucketName) {
        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build()
            );
            log.info("文件下载成功：{}/{}", bucketName, objectKey);
            return response;
        } catch (S3Exception e) {
            log.error("文件下载失败：{}", e.getMessage());
            throw new StorageException("文件不存在或下载失败");
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectKey) {
        deleteFile(objectKey, properties.getBucket().getDefaultBucket());
    }

    public void deleteFile(String objectKey, String bucketName) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build());
        log.info("文件删除成功：{}/{}", bucketName, objectKey);
    }

    /**
     * 生成预签名 URL（用于前端直传/下载）
     */
    public String generatePresignedUrl(String objectKey, HttpMethod method) {
        return generatePresignedUrl(objectKey, method, properties.getBucket().getDefaultBucket());
    }

    public String generatePresignedUrl(String objectKey, HttpMethod method, String bucketName) {
        Instant expiration = Instant.now().plusSeconds(properties.getPresigned().getExpireSeconds());

        if (method == HttpMethod.GET) {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(properties.getPresigned().getExpireSeconds()))
                .getObjectRequest(builder -> builder.bucket(bucketName).key(objectKey))
                .build();
            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } else if (method == HttpMethod.PUT) {
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(properties.getPresigned().getExpireSeconds()))
                .putObjectRequest(builder -> builder.bucket(bucketName).key(objectKey))
                .build();
            return s3Presigner.presignPutObject(presignRequest).url().toString();
        }
        throw new IllegalArgumentException("不支持的 HTTP 方法：" + method);
    }

    /**
     * 检查文件是否存在
     */
    public boolean exists(String objectKey) {
        return exists(objectKey, properties.getBucket().getDefaultBucket());
    }

    public boolean exists(String objectKey, String bucketName) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build());
            return true;
        } catch (S3Exception e) {
            return e.statusCode() == 404;
        }
    }

    /**
     * 获取文件信息
     */
    public FileInfo getFileInfo(String objectKey) {
        return getFileInfo(objectKey, properties.getBucket().getDefaultBucket());
    }

    public FileInfo getFileInfo(String objectKey, String bucketName) {
        HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build());
        FileInfo info = new FileInfo();
        info.setObjectKey(objectKey);
        info.setBucket(bucketName);
        info.setSize(response.contentLength());
        info.setContentType(response.contentType());
        info.setLastModified(response.lastModified());
        return info;
    }

    /**
     * 列举桶中文件
     */
    public List<FileInfo> listFiles(String prefix) {
        return listFiles(prefix, properties.getBucket().getDefaultBucket());
    }

    public List<FileInfo> listFiles(String prefix, String bucketName) {
        List<FileInfo> files = new ArrayList<>();
        ListObjectsV2Response response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(prefix)
            .build());
        for (S3Object obj : response.contents()) {
            FileInfo info = new FileInfo();
            info.setObjectKey(obj.key());
            info.setSize(obj.size());
            info.setLastModified(obj.lastModified());
            files.add(info);
        }
        return files;
    }

    // 工具方法
    private void validateFileSize(long size) {
        if (size > properties.getUpload().getMaxSize()) {
            throw new StorageException("文件大小超过限制：" + properties.getUpload().getMaxSize() + " 字节");
        }
    }

    private void validateFileType(String contentType) {
        if (contentType == null || !properties.getUpload().getAllowedTypes().contains(contentType)) {
            throw new StorageException("不支持的文件类型：" + contentType);
        }
    }

    private String generateObjectKey(String originalFilename) {
        String ext = originalFilename != null && originalFilename.contains(".")
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/"
            + UUID.randomUUID().toString().replace("-", "") + ext;
    }

    public static class FileInfo {
        private String objectKey;
        private String bucket;
        private Long size;
        private String contentType;
        private Instant lastModified;

        public String getObjectKey() {
            return objectKey;
        }

        public void setObjectKey(String objectKey) {
            this.objectKey = objectKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public Long getSize() {
            return size;
        }

        public void setSize(Long size) {
            this.size = size;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public Instant getLastModified() {
            return lastModified;
        }

        public void setLastModified(Instant lastModified) {
            this.lastModified = lastModified;
        }
    }

    public static class StorageException extends RuntimeException {
        public StorageException(String message) {
            super(message);
        }
    }
}