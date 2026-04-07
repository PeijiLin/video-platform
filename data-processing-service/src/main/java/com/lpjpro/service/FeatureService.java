package com.lpjpro.service;

import java.util.Map;

public interface FeatureService {

    /**
     * 获取实时特征（短期行为，24小时内）
     * @param userId 用户ID
     * @return 特征权重映射
     */
    Map<String, Double> getRealtimeFeatures(Long userId);

    /**
     * 获取长期特征（30天聚合数据）
     * @param userId 用户ID
     * @return 特征权重映射
     */
    Map<String, Double> getLongTermFeatures(Long userId);

    /**
     * 更新用户特征
     * @param userId 用户ID
     */
    void updateUserFeature(Long userId);
}
