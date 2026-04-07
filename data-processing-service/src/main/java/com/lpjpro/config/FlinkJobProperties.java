package com.lpjpro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "flink.job.hotvideo")
public class FlinkJobProperties {
    private String kafkaBootstrapServers;
    private String kafkaTopic;
    private String consumerGroupId;
    private int windowSizeMinutes;      // 窗口大小（分钟）
    private int slideIntervalMinutes;   // 滑动间隔（分钟）
    private int topN;               // 输出前几名
    private int kafkaPartitions; // 分区数
}
