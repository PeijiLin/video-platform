package com.lpjpro.model.video.VO;

import lombok.Data;

import java.io.Serializable;

@Data
public class GetVideoVO implements Serializable {
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
     * 视频标题
     */
    private String title;

    /*
     * 封面图URL
     */
    private String coverUrl;

    /**
     * 视频存储路径（如云存储）
    */
    private String videoUrl;

    /**
     * 视频时长（秒）
     */
    private Integer duration;


    /**
     * 标签（逗号分隔，如：#旅行,#美食）
     */
    private String tags;


    /**
     * 播放量
     */
    private Long views;

    /**
     * 点赞数
     */
    private Long likes;

    /**
     * 视频描述
     */
    private String description;

    /**
     * 评论数
     */
    private Long collections;
}
