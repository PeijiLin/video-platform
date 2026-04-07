package com.lpjpro.controller;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.pojo.DTO.RecommendationFeedbackRequest;
import com.lpjpro.pojo.VO.RecommendedVideo;
import com.lpjpro.service.RecommendationService;
import com.lpjpro.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 视频推荐控制器
 */
@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
    
    @Resource
    private RecommendationService recommendationService;
    
    /**
     * 获取个性化推荐视频列表
     * @param size 推荐数量
     * @return 推荐视频列表
     */
    @GetMapping("/personalized")
    public BaseResponse<List<RecommendedVideo>> getPersonalizedRecommendations(
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        return ResultUtils.success(recommendationService.getPersonalizedRecommendations(size));
    }
    
    /**
     * 获取相似视频推荐
     * @param videoId 视频ID
     * @param size 推荐数量
     * @return 推荐视频列表
     */
    @GetMapping("/similar/{videoId}")
    public BaseResponse<List<RecommendedVideo>> getSimilarVideos(
            @PathVariable Long videoId,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResultUtils.success(null);
    }
    

    /**
     * 处理用户反馈（用于实时更新用户兴趣模型）
     * @param request 反馈请求
     * @return 处理结果
     */
    @PostMapping("/feedback")
    public BaseResponse<Boolean> processFeedback(
            @RequestBody RecommendationFeedbackRequest request) {
        return null;
    }
}