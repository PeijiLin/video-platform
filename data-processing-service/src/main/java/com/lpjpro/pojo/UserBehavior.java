package com.lpjpro.pojo;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户行为日志表
 * @TableName user_behavior
 */
@Data
public class UserBehavior implements Serializable {
    /**
     * 行为唯一ID
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

    private Long categoryId;

    /**
     * 行为类型（play, like, comment, share, pause）
     */
    private String behaviorType;

    /**
     * 行为发生时间
     */
    private Date timestamp;

    /**
     * 行为持续时间（秒，如播放时长）
     */
    private Integer watchVideoTime;

    /**
     * 地理位置（IP解析）
     */
    private String ipAddress;

    /**
     * 记录创建时间
     */
    private Date createdTime;
}