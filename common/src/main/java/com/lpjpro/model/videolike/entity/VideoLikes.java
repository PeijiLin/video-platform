package com.lpjpro.model.videolike.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 记录用户对视频的点赞行为
 * @TableName video_likes
 */
@Data
public class VideoLikes implements Serializable {
    /**
     * 点赞记录的唯一ID
     */
    private Long id;

    /**
     * 用户ID，标识点赞的用户
     */
    private Long userId;

    /**
     * 视频ID，标识被点赞的视频
     */
    private Long videoId;

    /**
     * 点赞时间，记录用户点赞的时间
    */
    private Date likedTime;

    /**
     * 是否点赞
     */
    private Integer isActive;

    /**
     * 是否已删除（0：存在； 1：删除）
     */
    private Integer isDelete;
}