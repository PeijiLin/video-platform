package com.lpjpro.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lpjpro.model.preference.entity.UserCategoryPreference;

import java.util.List;
import java.util.Map;

/**
 * 偏好计算服务
 */
public interface PreferenceComputeService extends IService<UserCategoryPreference> {

    /**
     * 根据视频ID实时更新用户该分类的偏好分（被 interaction-service Feign 调用）
     * @param userId    用户ID
     * @param videoId   视频ID（用于获取分类ID）
     * @param behaviorType 行为类型：like, favorite, comment, play
     */
    void updatePreferenceOnBehavior(Long userId, Long videoId, String behaviorType);

    /**
     * 为单个用户重新计算所有分类的偏好分
     */
    void computeSingleUserPreference(Long userId);

    /**
     * 全量计算所有用户的偏好分（每日定时任务）
     */
    void computeAllUserPreferences();

    /**
     * 获取用户各分类的偏好得分映射
     */
    Map<Long, Double> getUserCategoryScores(Long userId);
}
