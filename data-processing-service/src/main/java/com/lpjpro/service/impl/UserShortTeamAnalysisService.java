package com.lpjpro.service.impl;

import com.clickhouse.client.api.Client;
import com.google.gson.reflect.TypeToken;
import com.lpjpro.config.FlinkJobProperties;
import com.lpjpro.pojo.UserBehaviorRequest;
import com.lpjpro.pojo.UserShortTermData;
import com.lpjpro.sink.ClickHouseSink;
import com.lpjpro.utils.JSONUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Slf4j
@Service
public class UserShortTeamAnalysisService {

    private static final Long DEFAULT_USER_ID = 0L;
    private static final String DEFAULT_BEHAVIOR_TYPE = "unknown";

    @Resource
    private FlinkJobProperties properties;

    @Resource
    private Client client;

    public void startJob() throws Exception {
        // 配置流环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 启用 Checkpoint，确保 Exactly-Once 语义
        env.enableCheckpointing(5000); // 每 5 秒触发一次 Checkpoint
        env.getCheckpointConfig().setCheckpointStorage("file:///tmp/flink/checkpoints");

        // 配置Kafka连接属性
        Properties kafkaProps = createKafkaProperties();

        // 配置Kafka数据源
        KafkaSource<String> kafkaSource = createKafkaSource(kafkaProps);

        // 创建数据流
        DataStreamSource<String> dataStreamSource = env.fromSource(
                kafkaSource, 
                WatermarkStrategy.noWatermarks(),
                "Kafka Source"
        ).setParallelism(properties.getKafkaPartitions());

        // 解析JSON并处理数据
        DataStream<UserBehaviorRequest> processedStream = parseAndProcessData(dataStreamSource);

        // 按用户ID和行为类型分组并统计
        DataStream<UserShortTermData> analysisResult = processedStream
                .keyBy(createKeySelector())
                .window(TumblingEventTimeWindows.of(Time.minutes(10)))
                .process(new UserBehaviorWindowProcessor());

        // 将结果写入ClickHouse
        analysisResult.addSink(new ClickHouseSink("user_shortterm_features",client));
        // 启动任务
        env.execute("User Behavior Analysis Job");
    }

    /**
     * 创建Kafka连接属性
     */
    private Properties createKafkaProperties() {
        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("metadata.max.age.ms", "30000");
        kafkaProps.setProperty("request.timeout.ms", "30000");
        kafkaProps.setProperty("session.timeout.ms", "30000");
        // 注意：不要在这里设置反序列化器，因为已经在KafkaSource中使用了setValueOnlyDeserializer
        return kafkaProps;
    }

    /**
     * 创建Kafka数据源
     */
    private KafkaSource<String> createKafkaSource(Properties kafkaProps) {
        return KafkaSource.<String>builder()
                .setBootstrapServers(properties.getKafkaBootstrapServers())
                .setGroupId(properties.getConsumerGroupId())
                .setTopics(properties.getKafkaTopic())
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setProperties(kafkaProps)
                .build();
    }

    /**
     * 解析JSON并处理数据
     */
    private DataStream<UserBehaviorRequest> parseAndProcessData(DataStreamSource<String> source) {
        // 解析JSON并处理空值
        SingleOutputStreamOperator<UserBehaviorRequest> parsedStream = source
                .flatMap(new UserBehaviorJsonParser())
                .returns(UserBehaviorRequest.class);

        // 分配时间戳和水印
        SingleOutputStreamOperator<UserBehaviorRequest> timestampedStream = parsedStream
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<UserBehaviorRequest>forMonotonousTimestamps()
                                .withTimestampAssigner((element, timestamp) -> 
                                        element.getTimestamp() != null ? element.getTimestamp().getTime() : 0L)
                );

        // 添加日志记录和过滤无效数据
        return timestampedStream
                .map(new LoggingMapper())
                .filter(new ValidUserBehaviorFilter());
    }

    /**
     * 创建KeySelector，用于按用户ID和行为类型分组
     */
    private KeySelector<UserBehaviorRequest, Tuple2<Long, String>> createKeySelector() {
        return new KeySelector<UserBehaviorRequest, Tuple2<Long, String>>() {
            @Override
            public Tuple2<Long, String> getKey(UserBehaviorRequest value) {
                // 确保userId和behaviorType不为null，使用默认值代替null
                Long userId = value.getUserId() != null ? value.getUserId() : DEFAULT_USER_ID;
                String behaviorType = value.getBehaviorType() != null ? value.getBehaviorType() : DEFAULT_BEHAVIOR_TYPE;
                return Tuple2.of(userId, behaviorType);
            }
        };
    }

    /**
     * JSON解析器，将JSON字符串转换为UserBehaviorRequest对象
     */
    private static class UserBehaviorJsonParser implements FlatMapFunction<String, UserBehaviorRequest> {
        @Override
        public void flatMap(String value, Collector<UserBehaviorRequest> out) {
            try {
                log.info("接收到消息: {}", value);
                Type type = new TypeToken<List<UserBehaviorRequest>>() {
                }.getType();
                List<UserBehaviorRequest> list = JSONUtils.fromJson(value, type);
                
                if (list == null || list.isEmpty()) {
                    log.warn("解析JSON后得到空列表: {}", value);
                    return;
                }
                
                for (UserBehaviorRequest request : list) {
                    if (request == null) {
                        log.warn("列表中包含null元素");
                        continue;
                    }
                    
                    // 验证必要字段
                    if (request.getTimestamp() == null) {
                        log.error("请求中timestamp为null: {}", request);
                        continue;
                    }
                    
                    // 处理可能为空的字段，设置默认值
                    if (request.getUserId() == null) {
                        request.setUserId(DEFAULT_USER_ID);
                        log.info("设置默认userId={}, 原始请求: {}", DEFAULT_USER_ID, request);
                    }
                    
                    if (request.getBehaviorType() == null) {
                        request.setBehaviorType(DEFAULT_BEHAVIOR_TYPE);
                        log.info("设置默认behaviorType={}, 原始请求: {}", DEFAULT_BEHAVIOR_TYPE, request);
                    }
                    
                    // 确保所有必要字段都已设置
                    if (request.getUserId() != null && request.getBehaviorType() != null && request.getTimestamp() != null) {
                        out.collect(request);
                        log.debug("处理请求: {}", request);
                    } else {
                        log.error("请求缺少必要字段: {}", request);
                    }
                }
            } catch (Exception e) {
                log.error("解析JSON失败: {}", value, e);
            }
        }
    }

    /**
     * 日志记录Mapper，记录处理过程中的数据
     */
    private static class LoggingMapper implements MapFunction<UserBehaviorRequest, UserBehaviorRequest> {
        @Override
        public UserBehaviorRequest map(UserBehaviorRequest record) {
            log.debug("处理记录，userId={}, behaviorType={}", record.getUserId(), record.getBehaviorType());
            return record;
        }
    }

    /**
     * 有效用户行为过滤器，过滤掉无效的数据
     */
    private static class ValidUserBehaviorFilter implements FilterFunction<UserBehaviorRequest> {
        @Override
        public boolean filter(UserBehaviorRequest userBehavior) {
            boolean isValid = userBehavior != null && 
                              userBehavior.getUserId() != null && 
                              userBehavior.getBehaviorType() != null && 
                              userBehavior.getTimestamp() != null;
            
            if (!isValid) {
                log.warn("过滤无效数据: {}", userBehavior);
            }
            
            return isValid;
        }
    }

    /**
     * 用户行为窗口处理器，统计窗口内的用户行为数据
     */
    private static class UserBehaviorWindowProcessor extends ProcessWindowFunction<UserBehaviorRequest, UserShortTermData, Tuple2<Long, String>, TimeWindow> {
        @Override
        public void process(Tuple2<Long, String> key, Context context, Iterable<UserBehaviorRequest> elements, Collector<UserShortTermData> out) {
            int watchCount = 0;
            int likeCount = 0;
            int shareCount = 0;
            int commentCount = 0;
            long totalWatchTime = 0;
            log.info("====");
            // 使用key中的行为类型来确定统计哪种行为
            String behaviorType = key.f1;
            
            // 根据行为类型预先增加对应计数
            switch (behaviorType) {
                case "play":
                    // 对于播放行为，我们需要统计观看次数和总观看时间
                    for (UserBehaviorRequest behavior : elements) {
                        watchCount++;
                        if (behavior.getWatchVideoTime() != null) {
                            totalWatchTime += behavior.getWatchVideoTime();
                        }
                    }
                    break;
                case "like":
                    // 对于点赞行为，只需统计点赞次数
                    for (UserBehaviorRequest ignored : elements) {
                        likeCount++;
                    }
                    break;
                case "share":
                    // 对于分享行为，只需统计分享次数
                    for (UserBehaviorRequest ignored : elements) {
                        shareCount++;
                    }
                    break;
                case "comment":
                    // 对于评论行为，只需统计评论次数
                    for (UserBehaviorRequest ignored : elements) {
                        commentCount++;
                    }
                    break;
                default:
                    log.info("未知的行为类型: {}", behaviorType);
                    break;
            }
            
            // 计算平均观看时间
            double avgWatchTime = watchCount == 0 ? 0.0 : ((double) totalWatchTime) / watchCount;
            
            // 构建结果对象
            UserShortTermData result = UserShortTermData.builder()
                    .userId(Objects.toString(key.f0, "0")) // 确保userId不为null
                    .featureType("category")
                    .featureValue(Objects.toString(key.f1, DEFAULT_BEHAVIOR_TYPE)) // 确保featureType不为null
                    .watchCount(watchCount)
                    .likeCount(likeCount)
                    .shareCount(shareCount)
                    .commentCount(commentCount)
                    .avgWatchTime((int) avgWatchTime)
                    .date(new Date())
                    .build();

            log.info("窗口处理结果: {}", result);
            out.collect(result);
        }
    }
}