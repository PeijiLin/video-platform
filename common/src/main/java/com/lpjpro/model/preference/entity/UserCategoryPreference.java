package com.lpjpro.model.preference.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户分类偏好表（替代 ClickHouse user_longterm_features）
 * @TableName user_category_preference
 */
@Data
public class UserCategoryPreference implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 播放次数
     */
    private Integer watchCount;

    /**
     * 点赞次数
     */
    private Integer likeCount;

    /**
     * 评论次数
     */
    private Integer commentCount;

    /**
     * 收藏次数
     */
    private Integer favoriteCount;

    /**
     * 该分类下累计播放时长（秒）
     */
    private Integer totalWatchTime;

    /**
     * 加权偏好分
     * 公式：watch_count * 1.0 + like_count * 3.0 + comment_count * 5.0 + favorite_count * 4.0 + min(total_watch_time/60, 1000) * 0.5
     */
    private Double score;

    /**
     * 更新时间
     */
    private Date updatedTime;
}
