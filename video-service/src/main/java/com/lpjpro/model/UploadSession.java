package com.lpjpro.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

/**
 * 视频上传会话表
 * @TableName upload_session
 */
@TableName(value ="upload_session")
public class UploadSession implements Serializable {
    /**
     * 内部主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * MinIO S3 UploadId
     */
    private String uploadId;

    /**
     * 关联的视频 ID (草稿或正式)
     */
    private Long videoId;

    /**
     * MinIO 原始文件存储路径 (raw/...)
     */
    private String objectKey;

    /**
     * 存储桶
     */
    private String bucket;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 文件扩展名 (mp4, mov)
     */
    private String fileExt;

    /**
     * 文件 MD5/SHA256
     */
    private String fileHash;

    /**
     * 文件总大小 (字节)
     */
    private Long fileSize;

    /**
     * 预计总分片数
     */
    private Integer totalParts;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 已上传分片：[{ "partNumber": 1, "etag": "..." }]
     */
    private Object uploadedParts;

    /**
     * ACTIVE: 上传中，COMPLETED: 已合并，ABORTED: 已取消
     */
    private String status;

    /**
     *
     */
    private Date createdTime;

    /**
     *
     */
    private Date updatedTime;

    /**
     * 会话过期时间 (用于清理碎片)
     */
    private Date expireTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 内部主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 内部主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * MinIO S3 UploadId
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     * MinIO S3 UploadId
     */
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    /**
     * 关联的视频 ID (草稿或正式)
     */
    public Long getVideoId() {
        return videoId;
    }

    /**
     * 关联的视频 ID (草稿或正式)
     */
    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    /**
     * MinIO 原始文件存储路径 (raw/...)
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * MinIO 原始文件存储路径 (raw/...)
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * 存储桶
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * 存储桶
     */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * 原始文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 原始文件名
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * 文件扩展名 (mp4, mov)
     */
    public String getFileExt() {
        return fileExt;
    }

    /**
     * 文件扩展名 (mp4, mov)
     */
    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    /**
     * 文件 MD5/SHA256
     */
    public String getFileHash() {
        return fileHash;
    }

    /**
     * 文件 MD5/SHA256
     */
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    /**
     * 文件总大小 (字节)
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 文件总大小 (字节)
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 预计总分片数
     */
    public Integer getTotalParts() {
        return totalParts;
    }

    /**
     * 预计总分片数
     */
    public void setTotalParts(Integer totalParts) {
        this.totalParts = totalParts;
    }

    /**
     * 用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 用户 ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 已上传分片：[{ "partNumber": 1, "etag": "..." }]
     */
    public Object getUploadedParts() {
        return uploadedParts;
    }

    /**
     * 已上传分片：[{ "partNumber": 1, "etag": "..." }]
     */
    public void setUploadedParts(Object uploadedParts) {
        this.uploadedParts = uploadedParts;
    }

    /**
     * ACTIVE: 上传中，COMPLETED: 已合并，ABORTED: 已取消
     */
    public String getStatus() {
        return status;
    }

    /**
     * ACTIVE: 上传中，COMPLETED: 已合并，ABORTED: 已取消
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *
     */
    public Date getCreatedTime() {
        return createdTime;
    }

    /**
     * 
     */
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    /**
     *
     */
    public Date getUpdatedTime() {
        return updatedTime;
    }

    /**
     * 
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * 会话过期时间 (用于清理碎片)
     */
    public Date getExpireTime() {
        return expireTime;
    }

    /**
     * 会话过期时间 (用于清理碎片)
     */
    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        UploadSession other = (UploadSession) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUploadId() == null ? other.getUploadId() == null : this.getUploadId().equals(other.getUploadId()))
            && (this.getVideoId() == null ? other.getVideoId() == null : this.getVideoId().equals(other.getVideoId()))
            && (this.getObjectKey() == null ? other.getObjectKey() == null : this.getObjectKey().equals(other.getObjectKey()))
            && (this.getBucket() == null ? other.getBucket() == null : this.getBucket().equals(other.getBucket()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getFileExt() == null ? other.getFileExt() == null : this.getFileExt().equals(other.getFileExt()))
            && (this.getFileHash() == null ? other.getFileHash() == null : this.getFileHash().equals(other.getFileHash()))
            && (this.getFileSize() == null ? other.getFileSize() == null : this.getFileSize().equals(other.getFileSize()))
            && (this.getTotalParts() == null ? other.getTotalParts() == null : this.getTotalParts().equals(other.getTotalParts()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getUploadedParts() == null ? other.getUploadedParts() == null : this.getUploadedParts().equals(other.getUploadedParts()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
            && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()))
            && (this.getExpireTime() == null ? other.getExpireTime() == null : this.getExpireTime().equals(other.getExpireTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUploadId() == null) ? 0 : getUploadId().hashCode());
        result = prime * result + ((getVideoId() == null) ? 0 : getVideoId().hashCode());
        result = prime * result + ((getObjectKey() == null) ? 0 : getObjectKey().hashCode());
        result = prime * result + ((getBucket() == null) ? 0 : getBucket().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getFileExt() == null) ? 0 : getFileExt().hashCode());
        result = prime * result + ((getFileHash() == null) ? 0 : getFileHash().hashCode());
        result = prime * result + ((getFileSize() == null) ? 0 : getFileSize().hashCode());
        result = prime * result + ((getTotalParts() == null) ? 0 : getTotalParts().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUploadedParts() == null) ? 0 : getUploadedParts().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getUpdatedTime() == null) ? 0 : getUpdatedTime().hashCode());
        result = prime * result + ((getExpireTime() == null) ? 0 : getExpireTime().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", uploadId=").append(uploadId);
        sb.append(", videoId=").append(videoId);
        sb.append(", objectKey=").append(objectKey);
        sb.append(", bucket=").append(bucket);
        sb.append(", fileName=").append(fileName);
        sb.append(", fileExt=").append(fileExt);
        sb.append(", fileHash=").append(fileHash);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", totalParts=").append(totalParts);
        sb.append(", userId=").append(userId);
        sb.append(", uploadedParts=").append(uploadedParts);
        sb.append(", status=").append(status);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", expireTime=").append(expireTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}