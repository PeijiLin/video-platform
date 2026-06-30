package com.lpjpro.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpjpro.api.video.VideoApi;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.api.video.CategoryFeignClient;
import com.lpjpro.mapper.UserBehaviorLogMapper;
import com.lpjpro.mapper.UserCategoryPreferenceMapper;
import com.lpjpro.model.behavior.entity.UserBehaviorLog;
import com.lpjpro.model.preference.entity.UserCategoryPreference;
import com.lpjpro.model.video.VO.GetVideoVO;
import com.lpjpro.service.PreferenceComputeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 偏好计算服务实现
 */
@Slf4j
@Service
public class PreferenceComputeServiceImpl extends ServiceImpl<UserCategoryPreferenceMapper, UserCategoryPreference>
    implements PreferenceComputeService {

    @Resource
    private UserCategoryPreferenceMapper preferenceMapper;

    @Resource
    private UserBehaviorLogMapper behaviorLogMapper;

    @Resource
    private VideoApi videoApi;

    @Resource
    private CategoryFeignClient categoryFeignClient;

    /**
     * 行为类型权重
     */
    private static final double WATCH_WEIGHT = 1.0;
    private static final double LIKE_WEIGHT = 3.0;
    private static final double COMMENT_WEIGHT = 5.0;
    private static final double FAVORITE_WEIGHT = 4.0;
    private static final double WATCH_TIME_WEIGHT = 0.5;
    private static final double WATCH_TIME_BONUS_CAP = 1000.0;

    @Override
    public void updatePreferenceOnBehavior(Long userId, Long videoId, String behaviorType) {
        if (userId == null || videoId == null || behaviorType == null) {
            return;
        }
        try {
            // 通过 Feign 获取视频分类ID
            BaseResponse<GetVideoVO> resp = videoApi.getById(videoId);
            if (resp == null || resp.getData() == null) {
                log.warn("无法获取视频信息, videoId={}", videoId);
                return;
            }
            Long categoryId = resp.getData().getCategoryId();
            if (categoryId == null) {
                return;
            }

            // 查询现有偏好记录
            UserCategoryPreference existing = preferenceMapper.selectByUserAndCategory(userId, categoryId);

            int watchCount = 0, likeCount = 0, commentCount = 0, favoriteCount = 0;
            int totalWatchTime = 0;

            if (existing != null) {
                watchCount = existing.getWatchCount() != null ? existing.getWatchCount() : 0;
                likeCount = existing.getLikeCount() != null ? existing.getLikeCount() : 0;
                commentCount = existing.getCommentCount() != null ? existing.getCommentCount() : 0;
                favoriteCount = existing.getFavoriteCount() != null ? existing.getFavoriteCount() : 0;
                totalWatchTime = existing.getTotalWatchTime() != null ? existing.getTotalWatchTime() : 0;
            }

            // 增量更新计数
            switch (behaviorType.toLowerCase()) {
                case "play" -> watchCount++;
                case "like" -> likeCount++;
                case "comment" -> commentCount++;
                case "favorite" -> favoriteCount++;
                default -> { }
            }

            double score = calculateScore(watchCount, likeCount, commentCount, favoriteCount, totalWatchTime);
            preferenceMapper.upsert(userId, categoryId, watchCount, likeCount, commentCount, favoriteCount, totalWatchTime, score);
            log.info("更新用户偏好: userId={}, categoryId={}, behaviorType={}", userId, categoryId, behaviorType);
        } catch (Exception e) {
            log.error("更新用户偏好失败: userId={}, videoId={}", userId, videoId, e);
        }
    }

    @Override
    public void computeSingleUserPreference(Long userId) {
        // 查询该用户最近30天的行为记录
        LambdaQueryWrapper<UserBehaviorLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBehaviorLog::getUserId, userId);
        List<UserBehaviorLog> logs = behaviorLogMapper.selectList(wrapper);

        if (logs.isEmpty()) {
            return;
        }

        // 按分类聚合
        Map<Long, CategoryStats> aggMap = new HashMap<>();
        for (UserBehaviorLog logEntry : logs) {
            Long catId = logEntry.getCategoryId();
            CategoryStats stats = aggMap.computeIfAbsent(catId, k -> new CategoryStats());
            stats.categoryId = catId;
            switch (logEntry.getBehaviorType().toLowerCase()) {
                case "play" -> { stats.watchCount++; stats.totalWatchTime += (logEntry.getWatchVideoTime() != null ? logEntry.getWatchVideoTime() : 0); }
                case "like" -> stats.likeCount++;
                case "comment" -> stats.commentCount++;
                case "favorite" -> stats.favoriteCount++;
            }
        }

        // 写入各分类偏好
        for (CategoryStats stats : aggMap.values()) {
            double score = calculateScore(stats.watchCount, stats.likeCount,
                    stats.commentCount, stats.favoriteCount, stats.totalWatchTime);
            preferenceMapper.upsert(userId, stats.categoryId, stats.watchCount,
                    stats.likeCount, stats.commentCount, stats.favoriteCount,
                    stats.totalWatchTime, score);
        }
        log.info("重新计算用户偏好完成: userId={}", userId);
    }

    @Override
    public void computeAllUserPreferences() {
        // 获取所有有行为记录的用户ID
        List<Long> userIds = behaviorLogMapper.selectList(null).stream()
                .map(UserBehaviorLog::getUserId)
                .distinct()
                .toList();

        for (Long userId : userIds) {
            try {
                computeSingleUserPreference(userId);
            } catch (Exception e) {
                log.error("计算用户偏好失败: userId={}", userId, e);
            }
        }
        log.info("全量偏好计算完成，共处理用户数: {}", userIds.size());
    }

    @Override
    public Map<Long, Double> getUserCategoryScores(Long userId) {
        LambdaQueryWrapper<UserCategoryPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCategoryPreference::getUserId, userId)
                .orderByDesc(UserCategoryPreference::getScore);
        List<UserCategoryPreference> list = baseMapper.selectList(wrapper);

        Map<Long, Double> result = new HashMap<>();
        for (UserCategoryPreference pref : list) {
            result.put(pref.getCategoryId(), pref.getScore());
        }
        return result;
    }

    /**
     * 计算加权偏好分
     * score = watch_count * 1.0 + like_count * 3.0 + comment_count * 5.0
     *       + favorite_count * 4.0 + min(total_watch_time/60, 1000) * 0.5
     */
    private double calculateScore(int watchCount, int likeCount, int commentCount,
                                   int favoriteCount, int totalWatchTime) {
        double watchTimeBonus = Math.min(totalWatchTime / 60.0, WATCH_TIME_BONUS_CAP) * WATCH_TIME_WEIGHT;
        return watchCount * WATCH_WEIGHT
                + likeCount * LIKE_WEIGHT
                + commentCount * COMMENT_WEIGHT
                + favoriteCount * FAVORITE_WEIGHT
                + watchTimeBonus;
    }

    /**
     * 聚合统计内部类
     */
    private static class CategoryStats {
        Long categoryId;
        int watchCount = 0;
        int likeCount = 0;
        int commentCount = 0;
        int favoriteCount = 0;
        int totalWatchTime = 0;
    }
}
