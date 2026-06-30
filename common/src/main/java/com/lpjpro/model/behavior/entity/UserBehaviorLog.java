package com.lpjpro.model.behavior.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户行为日志表（替代 ClickHouse user_behavior）
 * @TableName user_behavior_log
 */
@Data
public class UserBehaviorLog implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 视频ID
     */
    private Long videoId;

    /**
     * 视频分类ID
     */
    private Long categoryId;

    /**
     * 行为类型：play, like, comment, share, favorite
     */
    private String behaviorType;

    /**
     * 播放时长（秒）
     */
    private Integer watchVideoTime;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 创建时间
     */
    private Date createdTime;
}
