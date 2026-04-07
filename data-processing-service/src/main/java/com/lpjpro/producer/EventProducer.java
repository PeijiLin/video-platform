package com.lpjpro.producer;

import com.lpjpro.pojo.UserBehaviorRequest;
import com.lpjpro.utils.JSONUtils;
import jakarta.annotation.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventProducer {

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sentEvent(List<UserBehaviorRequest> userBehaviorRequests, String topic) {
        Long userId = userBehaviorRequests.get(0).getUserId();
        String data = JSONUtils.toJson(userBehaviorRequests);
        kafkaTemplate.send(topic, String.valueOf(userId), data);
    }
}
