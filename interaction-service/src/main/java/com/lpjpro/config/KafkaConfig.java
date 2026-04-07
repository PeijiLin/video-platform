package com.lpjpro.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_VIDEO_LIKES = "video-likes";
    public static final String TOPIC_VIDEO_LIKES_DLT = "video-likes-dlt";
    public static final String EVENT_TYPE_UPVOTE = "upvote";
    public static final String EVENT_TYPE_UNUPVOTE = "unupvote";

    @Bean
    public NewTopic videoLikesTopic() {
        return new NewTopic(TOPIC_VIDEO_LIKES, 3, (short) 1);
    }

    @Bean
    public NewTopic videoLikesDltTopic() {
        return new NewTopic(TOPIC_VIDEO_LIKES_DLT, 1, (short) 1);
    }
}
