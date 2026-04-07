package com.lpjpro.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitMultipartResponse {
    private Boolean instantUpload;       // 是否需要上传
    private String uploadId;            // MinIO UploadId
    private String fileUrl;             // 秒传时返回的文件访问地址
    private List<UploadPartInfo> uploadedParts; // 已上传分片信息
    private String objectKey;           // 存储路径
    private Long expireTime;            // 会话过期时间戳
}