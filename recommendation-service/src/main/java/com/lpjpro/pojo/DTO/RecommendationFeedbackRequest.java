package com.lpjpro.pojo.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 推荐反馈请求
 * 用于接收用户对推荐结果的反馈
 */
@Data
public class RecommendationFeedbackRequest {
    
    /**
     * 视频ID
     */
    @NotNull(message = "视频ID不能为空")
    private Long videoId;
    
    /**
     * 行为类型
     * play: 播放
     * like: 点赞
     * share: 分享
     * comment: 评论
     * finish: 看完
     * skip: 跳过
     */
    @NotNull(message = "行为类型不能为空")
    private String action;
    
    /**
     * 行为持续时间（秒）
     * 例如：观看时长
     */
    private Integer duration;
    
    /**
     * 额外信息
     * 例如：评论内容、跳过原因等
     */
    private String extraInfo;
}