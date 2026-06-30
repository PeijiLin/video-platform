package com.lpjpro.consumer;

import cn.hutool.core.bean.BeanUtil;
import com.google.gson.reflect.TypeToken;
import com.jthinking.common.util.ip.IPInfo;
import com.jthinking.common.util.ip.IPInfoUtils;
import com.lpjpro.model.behavior.entity.UserBehaviorLog;
import com.lpjpro.pojo.UserBehaviorRequest;
import com.lpjpro.service.UserBehaviorLogService;
import com.lpjpro.utils.JSONUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Kafka 消费者：将 user-behavior topic 的消息写入 MySQL user_behavior_log 表
 */
@Slf4j
@Component
public class EventConsumer {

    @Resource
    private UserBehaviorLogService userBehaviorLogService;

    @KafkaListener(topics = {"user-behavior"}, groupId = "adminGroup", concurrency = "5")
    public void onEvent(String event, Acknowledgment ack) {
        try {
            Type type = new TypeToken<List<UserBehaviorRequest>>() {}.getType();
            List<UserBehaviorRequest> requests = JSONUtils.fromJson(event, type);

            List<UserBehaviorLog> logs = new ArrayList<>();
            for (UserBehaviorRequest req : requests) {
                UserBehaviorLog logEntry = new UserBehaviorLog();
                BeanUtil.copyProperties(req, logEntry);

                // IP 地址解析（保留）
                if (req.getIpAddress() != null && !req.getIpAddress().isEmpty()) {
                    try {
                        IPInfo ipInfo = IPInfoUtils.getIpInfo(req.getIpAddress());
                        if (ipInfo != null) {
                            logEntry.setIpAddress(ipInfo.getCountry() + ipInfo.getProvince());
                        }
                    } catch (Exception e) {
                        log.warn("IP解析失败: {}", req.getIpAddress());
                    }
                }

                logs.add(logEntry);
            }

            userBehaviorLogService.batchSave(logs);
            log.info("Kafka消费成功，写入 {} 条行为日志", logs.size());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Kafka消费失败", e);
            throw new RuntimeException(e);
        }
    }
}
