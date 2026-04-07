package com.lpjpro.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "video.s3")
public class VideoStorageProperties {

    private String endpoint;          // MinIO/S3 端点
    private String region;            // 区域（MinIO 可填任意）
    private String accessKey;
    private String secretKey;

    private Bucket bucket = new Bucket();
    private Cdn cdn = new Cdn();
    private Presigned presigned = new Presigned();
    private Upload upload = new Upload();

    public static class Bucket {
        private String video = "videos";
        private String cover = "covers";
        private String temp = "temp";
        private String avatar = "avatars";

        public String getVideo() {
            return video;
        }

        public void setVideo(String video) {
            this.video = video;
        }

        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        public String getTemp() {
            return temp;
        }

        public void setTemp(String temp) {
            this.temp = temp;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }

    public static class Cdn {
        private Boolean enabled = false;
        private String domain;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }

    public static class Presigned {
        private Integer uploadExpireSeconds = 900;    // 上传 URL 有效期（默认 15 分钟）
        private Integer playExpireSeconds = 7200;     // 播放 URL 有效期（默认 2 小时）

        public Integer getUploadExpireSeconds() {
            return uploadExpireSeconds;
        }

        public void setUploadExpireSeconds(Integer uploadExpireSeconds) {
            this.uploadExpireSeconds = uploadExpireSeconds;
        }

        public Integer getPlayExpireSeconds() {
            return playExpireSeconds;
        }

        public void setPlayExpireSeconds(Integer playExpireSeconds) {
            this.playExpireSeconds = playExpireSeconds;
        }
    }

    public static class Upload {
        private Multipart multipart = new Multipart();

        public static class Multipart {
            private Integer threadPoolSize = 4;       // 线程池核心数
            private Integer queueCapacity = 1000;     // 任务队列容量
            private Long minPartSize = 5 * 1024 * 1024L; // 最小分片 5MB（S3 限制）
            private Long maxPartSize = 5 * 1024 * 1024 * 1024L; // 最大分片 5GB

            public Integer getThreadPoolSize() {
                return threadPoolSize;
            }

            public void setThreadPoolSize(Integer threadPoolSize) {
                this.threadPoolSize = threadPoolSize;
            }

            public Integer getQueueCapacity() {
                return queueCapacity;
            }

            public void setQueueCapacity(Integer queueCapacity) {
                this.queueCapacity = queueCapacity;
            }

            public Long getMinPartSize() {
                return minPartSize;
            }

            public void setMinPartSize(Long minPartSize) {
                this.minPartSize = minPartSize;
            }

            public Long getMaxPartSize() {
                return maxPartSize;
            }

            public void setMaxPartSize(Long maxPartSize) {
                this.maxPartSize = maxPartSize;
            }
        }

        public Multipart getMultipart() {
            return multipart;
        }

        public void setMultipart(Multipart multipart) {
            this.multipart = multipart;
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public Cdn getCdn() {
        return cdn;
    }

    public void setCdn(Cdn cdn) {
        this.cdn = cdn;
    }

    public Presigned getPresigned() {
        return presigned;
    }

    public void setPresigned(Presigned presigned) {
        this.presigned = presigned;
    }

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload = upload;
    }
}