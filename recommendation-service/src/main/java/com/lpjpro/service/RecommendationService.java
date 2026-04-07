package com.lpjpro.service;

import com.lpjpro.pojo.VO.RecommendedVideo;

import java.util.List;

/**
 * 推荐
 */
public interface RecommendationService {
    /**
     * 个性化推荐视频列表
     * @param size
     * @return
     */
    List<RecommendedVideo> getPersonalizedRecommendations(int size);
}
