package com.lpjpro.consumer;

import cn.hutool.core.bean.BeanUtil;
import com.clickhouse.client.api.Client;
import com.clickhouse.data.ClickHouseFormat;
import com.google.gson.reflect.TypeToken;
import com.jthinking.common.util.ip.IPInfo;
import com.jthinking.common.util.ip.IPInfoUtils;
import com.lpjpro.pojo.UserBehavior;
import com.lpjpro.pojo.UserBehaviorRequest;
import com.lpjpro.utils.JSONUtils;
import jakarta.annotation.Resource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventConsumer {

    @Resource
    private Client client;

    @KafkaListener(topics = {"user-behavior"}, groupId = "adminGroup", concurrency = "5")
    public void onEvent(String event, Acknowledgment ack) {
        try {
            Type type = new TypeToken<List<UserBehaviorRequest>>() {
            }.getType();
            List<UserBehaviorRequest> userBehaviorRequests = JSONUtils.fromJson(event, type);
            List<UserBehavior> userBehaviors = new ArrayList<>();
            userBehaviorRequests.forEach(userBehaviorRequest -> {

                IPInfo ipInfo = IPInfoUtils.getIpInfo(userBehaviorRequest.getIpAddress());
                UserBehavior userBehavior = new UserBehavior();
                BeanUtil.copyProperties(userBehaviorRequest, userBehavior);
                userBehavior.setIpAddress(ipInfo.getCountry() + ipInfo.getProvince());
                userBehaviors.add(userBehavior);
            });
            // 将 List<UserBehavior> 转为多行 JSON 字符串
            System.out.println(JSONUtils.toJson(userBehaviors));
            List<String> jsonLines = userBehaviors.stream()
                    .map(JSONUtils::toJson)
                    .collect(Collectors.toList());

            String finalJson = String.join("\n", jsonLines);// 每行一个 JSON 对象
//            System.out.println(finalJson);
            InputStream inputStream = new ByteArrayInputStream(finalJson.getBytes());
            client.insert("user_behavior", inputStream, ClickHouseFormat.JSONEachRow);
            ack.acknowledge();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
