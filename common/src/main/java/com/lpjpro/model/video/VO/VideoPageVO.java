package com.lpjpro.model.video.VO;

import lombok.Data;

import java.io.Serializable;

@Data
public class VideoPageVO implements Serializable {
    /**
     * 视频唯一ID
     */
    private Long id;

    /**
     * 发布者ID（外键）
     */
    private Long userId;

    /**
     * 视频种类
     */
    private Long categoryId;

    /**
     * 用户名
     */
    private String username;

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
     * 视频时长（秒）
     */
    private Integer duration;


    /**
     * 播放量
     */
    private Long views;

    /**
     * 收藏数
     */
    private Long collections;

    /**
     * 是否点赞
     */
    private Boolean isLiked;

    /**
     * 是否收藏
     */
    private Boolean isFavorited;

    /**
     * 点赞数
     */
    private Long likes;
}
