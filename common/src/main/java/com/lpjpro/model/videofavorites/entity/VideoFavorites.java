package com.lpjpro.model.videofavorites.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 记录用户对视频的收藏行为
 * @TableName video_favorites
 */
@Data
public class VideoFavorites implements Serializable {
    /**
     * 收藏记录的唯一ID
     */
    private Long id;

    /**
     * 用户ID，标识收藏视频的用户
     */
    private Long userId;

    /**
     * 视频ID，标识被收藏的视频
     */
    private Long videoId;

    /**
     * 收藏时间，记录用户收藏的时间
     */
    private Date favoritedTime;

    /**
     * 是否收藏
     */
    private Integer isActive;

    /**
     * 是否删除（0：存在；1：删除）
     */
    private Integer isDelete;
}