package com.lpjpro.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.data_formats.ClickHouseBinaryFormatReader;
import com.clickhouse.client.api.query.QueryResponse;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.feign.CategoryFeignClient;
import com.lpjpro.model.category.VO.CategoryVO;
import com.lpjpro.model.category.entity.Category;
import com.lpjpro.model.userpicture.entity.UserPicture;
import com.lpjpro.service.FeatureService;
import com.lpjpro.service.UserPictureService;
import com.lpjpro.utils.JSONUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FeatureServiceImpl implements FeatureService {

    @Resource
    private Client client;
    @Resource
    private CategoryFeignClient categoryFeignClient;

    @Resource
    private UserPictureService userPictureService;

    /**
     * 获取实时特征（短期行为，24小时内）
     * 数据源：ClickHouse（实时更新）
     */
    @Override
    public Map<String, Double> getRealtimeFeatures(Long userId) {
        String sql = """
            SELECT 
                featureType,
                featureValue,
                sum(weight) AS total_weight
            FROM user_shortterm_features
            WHERE 
                userId = {userId:UInt64} 
                AND timestamp >= now() - INTERVAL 24 HOUR
                AND expire_time > now()
            GROUP BY featureType, featureValue
            ORDER BY totalWeight DESC
            LIMIT 100
            """;
        Map<String, Object> param = new HashMap<>();
        param.put("userId",userId);
        try (QueryResponse response = client.query(sql, param).get()){

            ClickHouseBinaryFormatReader reader = client.newBinaryFormatReader(response);
            Map<String, Double> features = new HashMap<>();

            while (reader.hasNext()) {
                reader.next();
                String type = reader.getString(0);
                String value = reader.getString(1);
                double weight = reader.getDouble(2);

                // 使用类型和值组合作为特征键
                String featureKey = type + ":" + value;
                features.put(featureKey, weight);
            }
            return features;
        } catch (Exception e) {
            // 发生异常时返回空特征集
            return new HashMap<>();
        }
    }

    /**
     * 获取长期特征（30天聚合数据）
     * 数据源：ClickHouse（每日离线计算）
     */
    @Override
    public Map<String, Double> getLongTermFeatures(Long userId) {
        String sql = """
                SELECT 
                    featureType,
                    feature_value,
                    SUM(watch_count) * 0.5 
                    + SUM(like_count) * 0.3 
                    + SUM(share_count) * 0.2 AS score
                FROM user_longterm_features
                WHERE 
                    user_id = {userId:UInt64} 
                    AND date >= today() - 30
                GROUP BY feature_type, feature_value
                ORDER BY score DESC
                LIMIT 100
                """;
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        try (QueryResponse response = client.query(sql, param).get()) {

            ClickHouseBinaryFormatReader reader = client.newBinaryFormatReader(response);
            Map<String, Double> features = new HashMap<>();

            while (reader.hasNext()) {
                reader.next();
                String type = reader.getString(0);
                String value = reader.getString(1);
                double weight = reader.getDouble(2);

                // 使用类型和值组合作为特征键
                String featureKey = type + ":" + value;
                features.put(featureKey, weight);
            }
            return features;
        } catch (Exception e) {
            // 发生异常时返回空特征集
            return new HashMap<>();
        }
    }


    /**
     * 更新用户特征
     * 暂时只有种类
     */
    @Override
    public void updateUserFeature(Long userId) {
        Map<String, Double> userFeature = new HashMap<>();

        // 获取用户特征（实时+长期）
        Map<String, Double> userRealtimeFeatures = getRealtimeFeatures(userId);
        Map<String, Double> userLongTermFeatures = getLongTermFeatures(userId);

        List<CategoryVO> categories = categoryFeignClient.allCategory().getData();
        List<Long> categoryIds = categories.stream().map(CategoryVO::getId).toList();

        // 计算匹配分数（实时特征权重0.7，长期特征权重0.3）
        categoryIds.forEach(categoryId -> {
            if (userRealtimeFeatures.containsKey(String.valueOf(categoryId)) || userLongTermFeatures.containsKey(String.valueOf(categoryId))) {
                double feature = userRealtimeFeatures.getOrDefault(String.valueOf(categoryId), 0.0) * 0.6 + userLongTermFeatures.getOrDefault(String.valueOf(categoryId), 0.0) * 0.4;
                userFeature.put(String.valueOf(categoryId), feature);
            }
        });
        String json = JSONUtils.toJson(userFeature);
        LambdaQueryWrapper<UserPicture> userPictureLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userPictureLambdaQueryWrapper.eq(UserPicture::getUserId, userId);
        UserPicture userPicture = userPictureService.getOne(userPictureLambdaQueryWrapper);
//        用户特征不存在，创建
        if (userPicture == null) {
            UserPicture userPicture1 = new UserPicture();
            userPicture1.setUserId(userId);
            userPicture1.setUserFeatures(json);
            ThrowsUtils.throwIf(!userPictureService.save(userPicture1), ErrorCode.SYSTEM_ERROR);
        }
//        用户特征存在，更新
        userPicture.setUserFeatures(json);
        userPictureService.updateById(userPicture);
    }
}
