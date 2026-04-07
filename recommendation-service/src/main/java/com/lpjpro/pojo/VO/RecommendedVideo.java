package com.lpjpro.pojo.VO;

import lombok.Data;

/**
 * 推荐视频模型
 * 包含视频信息和推荐原因
 */
@Data
public class RecommendedVideo {
    
    /**
     * 视频ID
     */
    private Long videoId;
    
    /**
     * 视频标题
     */
    private String title;
    
    /**
     * 视频封面URL
     */
    private String coverUrl;
    
    /**
     * 视频URL
     */
    private String videoUrl;
    
    /**
     * 视频描述
     */
    private String description;
    
    /**
     * 视频分类ID
     */
    private Long categoryId;
    
    /**
     * 视频分类名称
     */
    private String categoryName;
    
    /**
     * 发布者ID
     */
    private Long userId;
    
    /**
     * 发布者用户名
     */
    private String username;
    
    /**
     * 推荐分数
     */
    private Double score;
    
    /**
     * 推荐原因
     */
    private String reason;
}