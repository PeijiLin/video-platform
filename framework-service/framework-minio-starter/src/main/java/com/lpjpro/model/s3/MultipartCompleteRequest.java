package com.lpjpro.model.s3;

import java.util.List;

public class MultipartCompleteRequest {
    private String uploadId;
    private String key;
    private String bucket;
    private List<PartInfo> parts;

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public List<PartInfo> getParts() {
        return parts;
    }

    public void setParts(List<PartInfo> parts) {
        this.parts = parts;
    }
}