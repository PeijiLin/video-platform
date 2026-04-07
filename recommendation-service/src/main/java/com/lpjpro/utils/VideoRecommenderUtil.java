package com.lpjpro.utils;

import com.google.gson.reflect.TypeToken;
import com.lpjpro.feign.UserPictureServiceFeignClient;
import com.lpjpro.model.userpicture.entity.UserPicture;
import com.lpjpro.pojo.model.UserSimilarity;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;

@Component
public class VideoRecommenderUtil {
    // 获取用户相似度前K名
    private static final int TOP_K_USERS = 10;
    // 推荐视频类型数量
    private static final int TOP_N_RECOMMENDATIONS = 10;

    private static final double MIN_RATING = 5.0;    // 最低评分阈值


    @Resource
    private UserPictureServiceFeignClient userPictureServiceFeignClient;

    /**
     * 核心推荐方法
     * @param targetUserId 目标用户ID
     * @return 视频类型到推荐分数的映射 (按分数降序排序)
     */
    public Map<String, Double> recommendVideos(long targetUserId) {
        // 1. 获取目标用户特征
        Map<String, Double> targetFeatures = getUserFeatures(targetUserId);

        // 2. 冷启动处理：无用户特征数据
        if (targetFeatures.isEmpty()) {
            return getPopularVideos();
        }

        // 3. 计算用户相似度并获取相似用户
        List<UserSimilarity> similarUsers = findSimilarUsers(targetUserId, targetFeatures);

        // 4. 冷启动处理：无相似用户
        if (similarUsers.isEmpty()) {
            return getPopularVideos();
        }

        // 5. 生成推荐结果
        return generateRecommendations(targetFeatures, similarUsers);
    }

    // 获取用户特征数据
    private Map<String, Double> getUserFeatures(long userId) {
        Map<String, Double> features = new HashMap<>();
        UserPicture userPicture = userPictureServiceFeignClient.getUserPicture(userId).getData();
        String userFeatures = userPicture.getUserFeatures();
        features = JSONUtils.fromJson(userFeatures, HashMap.class);
        return features;
    }

    // 寻找相似用户
    private List<UserSimilarity> findSimilarUsers(long targetUserId, Map<String, Double> targetFeatures) {
        List<UserSimilarity> similarities = new ArrayList<>();
        List<UserPicture> data = userPictureServiceFeignClient.getUserPictureList(targetUserId).getData();
        for (UserPicture datum : data) {
            Long userId = datum.getUserId();
            String userFeatures = datum.getUserFeatures();
            Type type = new TypeToken<Map<String, Double>>() {
            }.getType();
            Map<String, Double> map = JSONUtils.fromJson(userFeatures, type);
            Map<String, Double> features = new HashMap<>();
            for (String key : map.keySet()) {
                features.put(key, map.get(key));
            }
            double similarity = calculateCosineSimilarity(targetFeatures, features);
            similarities.add(new UserSimilarity(userId, similarity, features));
        }
        // 按相似度降序排序并取TOP-K
        similarities.sort(Comparator.comparingDouble(UserSimilarity::getSimilarity).reversed());
        return similarities.subList(0, Math.min(TOP_K_USERS, similarities.size()));
    }

    // 计算余弦相似度
    private static double calculateCosineSimilarity(Map<String, Double> vec1, Map<String, Double> vec2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        // 计算点积和范数
        for (String key : vec1.keySet()) {
            if (vec2.containsKey(key)) {
                dotProduct += vec1.get(key) * vec2.get(key);
            }
            norm1 += Math.pow(vec1.get(key), 2);
        }

        for (Double value : vec2.values()) {
            norm2 += Math.pow(value, 2);
        }

        // 避免除以零
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 生成推荐结果 (带分数)
     */
    private static Map<String, Double> generateRecommendations(
            Map<String, Double> targetFeatures,
            List<UserSimilarity> similarUsers) {

        // 存储所有候选视频的加权分数
        Map<String, Double> candidateScores = new HashMap<>();
        // 存储每个视频的相似用户计数
        Map<String, Integer> candidateCounts = new HashMap<>();

        // 聚合相似用户的评分
        for (UserSimilarity user : similarUsers) {
            double userSimilarity = user.getSimilarity();

            for (Map.Entry<String, Double> entry : user.getFeatures().entrySet()) {
                String videoType = entry.getKey();
                double rating = entry.getValue();

                // 跳过目标用户已经接触过的类型
                if (targetFeatures.containsKey(videoType)) {
                    continue;
                }

                // 只考虑高评分视频
                if (rating >= MIN_RATING) {
                    double weightedScore = rating * userSimilarity;

                    // 累加分数和计数
                    candidateScores.merge(videoType, weightedScore, Double::sum);
                    candidateCounts.merge(videoType, 1, Integer::sum);
                }
            }
        }

        // 计算平均加权分数
        Map<String, Double> finalScores = new HashMap<>();
        for (String videoType : candidateScores.keySet()) {
            double totalScore = candidateScores.get(videoType);
            int count = candidateCounts.get(videoType);
            finalScores.put(videoType, totalScore / count);
        }

        // 按分数降序排序
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(finalScores.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        // 创建有序结果映射 (保留排序)
        Map<String, Double> recommendations = new LinkedHashMap<>();
        int count = Math.min(TOP_N_RECOMMENDATIONS, sortedEntries.size());
        for (int i = 0; i < count; i++) {
            Map.Entry<String, Double> entry = sortedEntries.get(i);
            recommendations.put(entry.getKey(), entry.getValue());
        }

        return recommendations;
    }

    /**
     * 冷启动方案：获取热门视频 (带模拟分数)
     */
    private static Map<String, Double> getPopularVideos() {
        Map<String, Double> popularVideos = new LinkedHashMap<>();
        // 实际实现应查询数据库，这里使用模拟数据
        popularVideos.put("9", 9.8);
        popularVideos.put("10", 9.5);
        popularVideos.put("1", 9.2);
        popularVideos.put("3", 8.7);
        popularVideos.put("5", 8.5);
        return popularVideos;
    }
}
