package com.lpjpro.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.nacos.shaded.com.google.gson.Gson;
import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.command.CommandSettings;
import com.clickhouse.client.api.data_formats.ClickHouseBinaryFormatReader;
import com.clickhouse.client.api.query.QueryResponse;
import com.clickhouse.client.internal.google.common.reflect.TypeToken;
import com.lpjpro.constant.BaseUserInfo;
import com.lpjpro.feign.UserPictureServiceFeignClient;
import com.lpjpro.feign.VideoServiceFeignClient;
import com.lpjpro.model.userpicture.entity.UserPicture;
import com.lpjpro.model.video.DTO.RecommVideoRequest;
import com.lpjpro.model.video.entity.Video;
import com.lpjpro.pojo.VO.RecommendedVideo;
import com.lpjpro.service.RecommendationService;
import com.lpjpro.utils.VideoRecommenderUtil;
import jakarta.annotation.Resource;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class recommendationServiceImpl implements RecommendationService {

    @Resource
    private UserPictureServiceFeignClient userPictureServiceFeignClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private VideoServiceFeignClient videoServiceFeignClient;

    @Resource
    private VideoRecommenderUtil videoRecommenderUtil;

    @Resource
    private Client client;

    /**
     * 个性化推荐视频列表
     * @param size 数量
     * @return 视频列表
     */
    @Override
    public List<RecommendedVideo> getPersonalizedRecommendations(int size) {
        List<RecommendedVideo> recommendedVideos = new ArrayList<>();
        // 获取用户状态 未登录（返回热门视频）
        Long currentUserId = BaseUserInfo.getCurrentUserId();

        // 更新用户画像列表
        userPictureServiceFeignClient.updateUserPicture(currentUserId);

        // 登录（返回推荐视频）
        if (currentUserId !=null) {
//            UserPicture userPicture = userPictureServiceFeignClient.getUserPicture(currentUserId).getData();
//            String userFeatures = userPicture.getUserFeatures();
//            Gson gson = new Gson();
//            Map<String, Double> userFeature = gson.fromJson(userFeatures,new TypeToken<Map<String, Double>>(){}.getType());
            Map<String, Double> userFeature = videoRecommenderUtil.recommendVideos(currentUserId);

            Map<String, Integer> videoSum = new HashMap<>();
            // 计算各类视频的比例
            double sum = userFeature.values().stream().mapToDouble(value -> value).sum();
            userFeature.forEach((key,value) -> {
                videoSum.put(key, (int)(size * (value / sum)));
            });

            // 获取观看过的视频列表
            String sql = """
                    select videoId from user_behavior
                    where userId = {userId:UInt64}
                    """;
            Map<String, Object> params = new HashMap<>();
            params.put("userId", currentUserId);
            List<Long> videoIds = new ArrayList<>();
            try(QueryResponse response = client.query(sql, params, new CommandSettings()).get()) {
                ClickHouseBinaryFormatReader reader = client.newBinaryFormatReader(response);
                while (reader.hasNext()) {
                    reader.next();
                    long videoId = reader.getLong("videoId");
                    videoIds.add(videoId);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            RecommVideoRequest recommVideoRequest = new RecommVideoRequest();
            recommVideoRequest.setVideoSum(videoSum);
            recommVideoRequest.setVideoIds(videoIds);
            // 根据标签获取相应数量的视频(远程调用)
            List<List<Video>> lists = videoServiceFeignClient.getList(recommVideoRequest).getData();

            for (List<Video> videoList : lists) {
                for (Video video : videoList) {
                    RecommendedVideo recommendedVideo = new RecommendedVideo();
                    BeanUtil.copyProperties(video, recommendedVideo);
                    recommendedVideos.add(recommendedVideo);
                }
            }
        }
        // 推荐热点视频
        String hotVideoKey = "hot_videos";
        ListOperations<String, Object> stringObjectListOperations = redisTemplate.opsForList();
        List<Object> range = stringObjectListOperations.range(hotVideoKey, 0, 20);
        List<RecommendedVideo> hotVideos = new ArrayList<>();
        if (range != null) {
            range.forEach(value -> hotVideos.add((RecommendedVideo) value));
        }

        recommendedVideos.addAll(hotVideos);
        return recommendedVideos;
    }
}
