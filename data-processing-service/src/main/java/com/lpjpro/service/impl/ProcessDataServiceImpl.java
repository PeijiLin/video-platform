package com.lpjpro.service.impl;

import com.clickhouse.client.api.Client;
import com.clickhouse.client.api.data_formats.ClickHouseBinaryFormatReader;
import com.clickhouse.client.api.query.QueryResponse;
import com.clickhouse.data.ClickHouseFormat;
import com.lpjpro.pojo.UserLongTermData;
import com.lpjpro.service.ProcessDataService;
import com.lpjpro.utils.JSONUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessDataServiceImpl implements ProcessDataService {

    @Resource
    private Client client;

    @Resource
    private UserShortTeamAnalysisService userShortTeamAnalysisService;

    /**
     * 用户长期数据
     */
    @Override
    public void userLongTermData() {
        // 使用clickhouse聚合函数计算
        // 查询近三十天数据所有用户的列
        // TODO 计算优化
        String sql = """
                SELECT
                    userId,
                    categoryId,
                    countIf(behaviorType = 'play') AS watchCount,
                    countIf(behaviorType = 'like') AS likeCount,
                    countIf(behaviorType = 'share') AS shareCount,
                    countIf(behaviorType = 'comment') AS commentCount,
                    avg(if(behaviorType = 'play', watchVideoTime, NULL)) AS avgWatchTime
                FROM user_behavior
                WHERE timestamp >= today() - 30 -- 近30天数据
                GROUP BY userId, categoryId
                ORDER BY userId, watchCount DESC;
                """;
        List<UserLongTermData> userLongTermDataList = new ArrayList<>();
        try (QueryResponse queryResponse = client.query(sql).get()){
            ClickHouseBinaryFormatReader reader = client.newBinaryFormatReader(queryResponse);
            while (reader.hasNext()) {
                reader.next();
                String userId = String.valueOf(reader.getLong("userId"));
                String categoryId = String.valueOf(reader.getLong("categoryId"));
                Integer watchCount = reader.getInteger("watchCount");
                Integer likeCount = reader.getInteger("likeCount");
                Integer shareCount = reader.getInteger("shareCount");
                Integer commentCount = reader.getInteger("commentCount");
                Integer avgWatchTime = getNullableDouble(reader, "avgWatchTime");
                UserLongTermData userLongTermData = UserLongTermData.builder()
                        .userId(userId).featureType("category").featureValue(categoryId).watchCount(watchCount).likeCount(likeCount).shareCount(shareCount).commentCount(commentCount).avgWatchTime(avgWatchTime).build();
                userLongTermDataList.add(userLongTermData);
            }
            System.out.println(userLongTermDataList);
            String json = JSONUtils.toJson(userLongTermDataList);
            InputStream inputStream = new ByteArrayInputStream(json.getBytes());
            // 处理后的数据存入数据库
            client.insert("user_longterm_features", inputStream, ClickHouseFormat.JSONCompactStringsEachRow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static Integer getNullableDouble(ClickHouseBinaryFormatReader reader, String columnName) throws IOException {
        String obj = reader.getString(columnName);
        if (obj == null) {
            return null;
        }
        return (int) Double.parseDouble(obj);
    }

    /**
     * 用户短期数据
     */
    @Override
    public void userShortTermData() throws Exception {
        // flink实时计算
        userShortTeamAnalysisService.startJob();
    }

    @Override
    public void videoFeaturesData() {

    }

}
