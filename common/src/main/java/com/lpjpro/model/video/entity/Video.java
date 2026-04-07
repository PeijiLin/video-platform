package com.lpjpro.model.video.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频信息表
 * @TableName video
 */
public class Video implements Serializable {
    /**
     * 视频唯一ID
     */
    private Long id;

    /**
     * 发布者ID（外键）
     */
    private Long userId;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频描述
     */
    private String description;

    /**
     * 封面图URL
     */
    private String coverUrl;

    /**
     * 视频存储路径（如云存储）
     */
    private String videoUrl;

    /**
     * MinIO 原始文件路径 (用于重新转码/下载)
     */
    private String rawObjectKey;

    /**
     * 文件大小 (字节)
     */
    private Long fileSize;

    /**
     * 视频格式
     */
    private String fileFormat;

    /**
     * 分辨率
     */
    private String resolution;

    /**
     * 视频时长（秒）
     */
    private Integer duration;

    /**
     * 分类ID（外键）
     */
    private Long categoryId;

    /**
     * 标签（逗号分隔，如：#旅行,#美食）
     */
    private String tags;

    /**
     * 状态：0-审核中，1-已通过，2-已拒绝
     */
    private Integer status;

    /**
     * 转码状态：0-未开始，1-转码中，2-完成，3-失败
     */
    private Integer transcodeStatus;

    /**
     * 播放量
     */
    private Long views;

    /**
     * 点赞数
     */
    private Long likes;

    /**
     * 收藏数
     */
    private Long collections;

    /**
     * 评论数
     */
    private Long comments;

    /**
     * 上传时间
     */
    private Date createdTime;

    /**
     * 最后更新时间
     */
    private Date updatedTime;

    /**
     * 审核时间
     */
    private Date reviewedTime;

    /**
     * 审核员ID（外键）
     */
    private Long reviewerId;

    /**
     * 逻辑删除时间
     */
    private Date deletedTime;

    /**
     * 是否删除（0-未删除，1-删除）
     */
    private Integer isDelete;

    /**
     * 文件 MD5/SHA256 哈希
     */
    private String fileHash;

    private static final long serialVersionUID = 1L;

    /**
     * 视频唯一ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 视频唯一ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 发布者ID（外键）
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 发布者ID（外键）
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 视频标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 视频标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 视频描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 视频描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 封面图URL
     */
    public String getCoverUrl() {
        return coverUrl;
    }

    /**
     * 封面图URL
     */
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    /**
     * 视频存储路径（如云存储）
     */
    public String getVideoUrl() {
        return videoUrl;
    }

    /**
     * 视频存储路径（如云存储）
     */
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    /**
     * MinIO 原始文件路径 (用于重新转码/下载)
     */
    public String getRawObjectKey() {
        return rawObjectKey;
    }

    /**
     * MinIO 原始文件路径 (用于重新转码/下载)
     */
    public void setRawObjectKey(String rawObjectKey) {
        this.rawObjectKey = rawObjectKey;
    }

    /**
     * 文件大小 (字节)
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * 文件大小 (字节)
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * 视频格式
     */
    public String getFileFormat() {
        return fileFormat;
    }

    /**
     * 视频格式
     */
    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    /**
     * 分辨率
     */
    public String getResolution() {
        return resolution;
    }

    /**
     * 分辨率
     */
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    /**
     * 视频时长（秒）
     */
    public Integer getDuration() {
        return duration;
    }

    /**
     * 视频时长（秒）
     */
    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    /**
     * 分类ID（外键）
     */
    public Long getCategoryId() {
        return categoryId;
    }

    /**
     * 分类ID（外键）
     */
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * 标签（逗号分隔，如：#旅行,#美食）
     */
    public String getTags() {
        return tags;
    }

    /**
     * 标签（逗号分隔，如：#旅行,#美食）
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * 状态：0-审核中，1-已通过，2-已拒绝
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 状态：0-审核中，1-已通过，2-已拒绝
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 转码状态：0-未开始，1-转码中，2-完成，3-失败
     */
    public Integer getTranscodeStatus() {
        return transcodeStatus;
    }

    /**
     * 转码状态：0-未开始，1-转码中，2-完成，3-失败
     */
    public void setTranscodeStatus(Integer transcodeStatus) {
        this.transcodeStatus = transcodeStatus;
    }

    /**
     * 播放量
     */
    public Long getViews() {
        return views;
    }

    /**
     * 播放量
     */
    public void setViews(Long views) {
        this.views = views;
    }

    /**
     * 点赞数
     */
    public Long getLikes() {
        return likes;
    }

    /**
     * 点赞数
     */
    public void setLikes(Long likes) {
        this.likes = likes;
    }

    /**
     * 收藏数
     */
    public Long getCollections() {
        return collections;
    }

    /**
     * 收藏数
     */
    public void setCollections(Long collections) {
        this.collections = collections;
    }

    /**
     * 评论数
     */
    public Long getComments() {
        return comments;
    }

    /**
     * 评论数
     */
    public void setComments(Long comments) {
        this.comments = comments;
    }

    /**
     * 上传时间
     */
    public Date getCreatedTime() {
        return createdTime;
    }

    /**
     * 上传时间
     */
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * 最后更新时间
     */
    public Date getUpdatedTime() {
        return updatedTime;
    }

    /**
     * 最后更新时间
     */
    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * 审核时间
     */
    public Date getReviewedTime() {
        return reviewedTime;
    }

    /**
     * 审核时间
     */
    public void setReviewedTime(Date reviewedTime) {
        this.reviewedTime = reviewedTime;
    }

    /**
     * 审核员ID（外键）
     */
    public Long getReviewerId() {
        return reviewerId;
    }

    /**
     * 审核员ID（外键）
     */
    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    /**
     * 逻辑删除时间
     */
    public Date getDeletedTime() {
        return deletedTime;
    }

    /**
     * 逻辑删除时间
     */
    public void setDeletedTime(Date deletedTime) {
        this.deletedTime = deletedTime;
    }

    /**
     * 是否删除（0-未删除，1-删除）
     */
    public Integer getIsDelete() {
        return isDelete;
    }

    /**
     * 是否删除（0-未删除，1-删除）
     */
    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    /**
     * 文件 MD5/SHA256 哈希
     */
    public String getFileHash() {
        return fileHash;
    }

    /**
     * 文件 MD5/SHA256 哈希
     */
    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
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
        Video other = (Video) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getCoverUrl() == null ? other.getCoverUrl() == null : this.getCoverUrl().equals(other.getCoverUrl()))
            && (this.getVideoUrl() == null ? other.getVideoUrl() == null : this.getVideoUrl().equals(other.getVideoUrl()))
            && (this.getRawObjectKey() == null ? other.getRawObjectKey() == null : this.getRawObjectKey().equals(other.getRawObjectKey()))
            && (this.getFileSize() == null ? other.getFileSize() == null : this.getFileSize().equals(other.getFileSize()))
            && (this.getFileFormat() == null ? other.getFileFormat() == null : this.getFileFormat().equals(other.getFileFormat()))
            && (this.getResolution() == null ? other.getResolution() == null : this.getResolution().equals(other.getResolution()))
            && (this.getDuration() == null ? other.getDuration() == null : this.getDuration().equals(other.getDuration()))
            && (this.getCategoryId() == null ? other.getCategoryId() == null : this.getCategoryId().equals(other.getCategoryId()))
            && (this.getTags() == null ? other.getTags() == null : this.getTags().equals(other.getTags()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getTranscodeStatus() == null ? other.getTranscodeStatus() == null : this.getTranscodeStatus().equals(other.getTranscodeStatus()))
            && (this.getViews() == null ? other.getViews() == null : this.getViews().equals(other.getViews()))
            && (this.getLikes() == null ? other.getLikes() == null : this.getLikes().equals(other.getLikes()))
            && (this.getCollections() == null ? other.getCollections() == null : this.getCollections().equals(other.getCollections()))
            && (this.getComments() == null ? other.getComments() == null : this.getComments().equals(other.getComments()))
            && (this.getCreatedTime() == null ? other.getCreatedTime() == null : this.getCreatedTime().equals(other.getCreatedTime()))
            && (this.getUpdatedTime() == null ? other.getUpdatedTime() == null : this.getUpdatedTime().equals(other.getUpdatedTime()))
            && (this.getReviewedTime() == null ? other.getReviewedTime() == null : this.getReviewedTime().equals(other.getReviewedTime()))
            && (this.getReviewerId() == null ? other.getReviewerId() == null : this.getReviewerId().equals(other.getReviewerId()))
            && (this.getDeletedTime() == null ? other.getDeletedTime() == null : this.getDeletedTime().equals(other.getDeletedTime()))
            && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
            && (this.getFileHash() == null ? other.getFileHash() == null : this.getFileHash().equals(other.getFileHash()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getCoverUrl() == null) ? 0 : getCoverUrl().hashCode());
        result = prime * result + ((getVideoUrl() == null) ? 0 : getVideoUrl().hashCode());
        result = prime * result + ((getRawObjectKey() == null) ? 0 : getRawObjectKey().hashCode());
        result = prime * result + ((getFileSize() == null) ? 0 : getFileSize().hashCode());
        result = prime * result + ((getFileFormat() == null) ? 0 : getFileFormat().hashCode());
        result = prime * result + ((getResolution() == null) ? 0 : getResolution().hashCode());
        result = prime * result + ((getDuration() == null) ? 0 : getDuration().hashCode());
        result = prime * result + ((getCategoryId() == null) ? 0 : getCategoryId().hashCode());
        result = prime * result + ((getTags() == null) ? 0 : getTags().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getTranscodeStatus() == null) ? 0 : getTranscodeStatus().hashCode());
        result = prime * result + ((getViews() == null) ? 0 : getViews().hashCode());
        result = prime * result + ((getLikes() == null) ? 0 : getLikes().hashCode());
        result = prime * result + ((getCollections() == null) ? 0 : getCollections().hashCode());
        result = prime * result + ((getComments() == null) ? 0 : getComments().hashCode());
        result = prime * result + ((getCreatedTime() == null) ? 0 : getCreatedTime().hashCode());
        result = prime * result + ((getUpdatedTime() == null) ? 0 : getUpdatedTime().hashCode());
        result = prime * result + ((getReviewedTime() == null) ? 0 : getReviewedTime().hashCode());
        result = prime * result + ((getReviewerId() == null) ? 0 : getReviewerId().hashCode());
        result = prime * result + ((getDeletedTime() == null) ? 0 : getDeletedTime().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        result = prime * result + ((getFileHash() == null) ? 0 : getFileHash().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", title=").append(title);
        sb.append(", description=").append(description);
        sb.append(", coverUrl=").append(coverUrl);
        sb.append(", videoUrl=").append(videoUrl);
        sb.append(", rawObjectKey=").append(rawObjectKey);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", fileFormat=").append(fileFormat);
        sb.append(", resolution=").append(resolution);
        sb.append(", duration=").append(duration);
        sb.append(", categoryId=").append(categoryId);
        sb.append(", tags=").append(tags);
        sb.append(", status=").append(status);
        sb.append(", transcodeStatus=").append(transcodeStatus);
        sb.append(", views=").append(views);
        sb.append(", likes=").append(likes);
        sb.append(", collections=").append(collections);
        sb.append(", comments=").append(comments);
        sb.append(", createdTime=").append(createdTime);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", reviewedTime=").append(reviewedTime);
        sb.append(", reviewerId=").append(reviewerId);
        sb.append(", deletedTime=").append(deletedTime);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", fileHash=").append(fileHash);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}