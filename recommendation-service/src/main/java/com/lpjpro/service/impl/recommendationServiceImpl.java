package com.lpjpro.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.lpjpro.api.video.CategoryFeignClient;
import com.lpjpro.api.video.VideoApi;
import com.lpjpro.constant.BaseUserInfo;
import com.lpjpro.mapper.UserBehaviorLogMapper;
import com.lpjpro.mapper.UserCategoryPreferenceMapper;
import com.lpjpro.model.behavior.entity.UserBehaviorLog;
import com.lpjpro.model.preference.entity.UserCategoryPreference;
import com.lpjpro.model.video.DTO.RecommVideoRequest;
import com.lpjpro.model.video.entity.Video;
import com.lpjpro.pojo.VO.RecommendedVideo;
import com.lpjpro.service.RecommendationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class recommendationServiceImpl implements RecommendationService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private VideoApi videoServiceFeignClient;

    @Resource
    private CategoryFeignClient categoryFeignClient;

    @Resource
    private UserCategoryPreferenceMapper userCategoryPreferenceMapper;

    @Resource
    private UserBehaviorLogMapper userBehaviorLogMapper;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 个性化推荐视频列表
     * @param size 数量
     * @return 视频列表
     */
    @Override
    public List<RecommendedVideo> getPersonalizedRecommendations(int size) {
        List<RecommendedVideo> recommendedVideos = new ArrayList<>();

        // 获取当前用户 ID
        Long currentUserId = BaseUserInfo.getCurrentUserId();

        // 未登录用户直接返回热门视频
        if (currentUserId == null) {
            return getHotVideos(size);
        }

        // 查询用户分类偏好
        List<UserCategoryPreference> preferences = userCategoryPreferenceMapper.selectByUserId(currentUserId);

        // 冷启动：无偏好记录，直接返回热门视频
        if (preferences == null || preferences.isEmpty()) {
            log.info("用户 {} 无偏好记录，返回热门视频", currentUserId);
            return getHotVideos(size);
        }

        // 计算分类配额
        Map<Long, Double> categoryScores = new HashMap<>();
        double totalScore = 0;
        for (UserCategoryPreference pref : preferences) {
            double score = pref.getScore() != null ? pref.getScore() : 0;
            categoryScores.put(pref.getCategoryId(), score);
            totalScore += score;
        }

        // 归一化为分类配额
        Map<Long, Integer> categoryQuota = new HashMap<>();
        for (Map.Entry<Long, Double> entry : categoryScores.entrySet()) {
            int quota = (int) (size * (entry.getValue() / totalScore));
            categoryQuota.put(entry.getKey(), Math.max(quota, 1));
        }

        // 查询已观看的视频（排除列表）
        Set<Long> watchedVideoIds = getWatchedVideoIds(currentUserId);

        // 按分类配额获取视频
        RecommVideoRequest request = new RecommVideoRequest();
        request.setVideoSum(categoryQuota.entrySet().stream()
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue)));
        request.setVideoIds(new ArrayList<>(watchedVideoIds));

        try {
            var response = videoServiceFeignClient.getList(request);
            if (response != null && response.getData() != null) {
                for (List<Video> videoList : response.getData()) {
                    for (Video video : videoList) {
                        RecommendedVideo recommendedVideo = new RecommendedVideo();
                        BeanUtil.copyProperties(video, recommendedVideo);
                        recommendedVideos.add(recommendedVideo);
                    }
                }
            }
        } catch (Exception e) {
            log.error("调用 video-service 失败", e);
        }

        // 末尾追加热门视频
        List<RecommendedVideo> hotVideos = getHotVideos(10);
        recommendedVideos.addAll(hotVideos);

        return recommendedVideos;
    }

    /**
     * 获取已观看的视频 ID 集合
     */
    private Set<Long> getWatchedVideoIds(Long userId) {
        List<UserBehaviorLog> logs = userBehaviorLogMapper.selectByUserIdAndType(userId, "play", 200);
        return logs.stream()
                .map(UserBehaviorLog::getVideoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 获取热门视频
     */
    private List<RecommendedVideo> getHotVideos(int limit) {
        String hotVideoKey = "hot_videos";
        ListOperations<String, Object> stringObjectListOperations = redisTemplate.opsForList();
        List<Object> range = stringObjectListOperations.range(hotVideoKey, 0, limit - 1);
        List<RecommendedVideo> hotVideos = new ArrayList<>();
        if (range != null) {
            for (Object value : range) {
                if (value instanceof RecommendedVideo) {
                    hotVideos.add((RecommendedVideo) value);
                } else if (value instanceof Video) {
                    RecommendedVideo recommendedVideo = new RecommendedVideo();
                    BeanUtil.copyProperties(value, recommendedVideo);
                    hotVideos.add(recommendedVideo);
                }
            }
        }
        return hotVideos;
    }
}
