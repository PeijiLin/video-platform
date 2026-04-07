package com.lpjpro.sink;

import com.clickhouse.client.api.insert.InsertResponse;
import com.clickhouse.client.api.insert.InsertSettings;
import com.lpjpro.pojo.UserShortTermData;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import com.clickhouse.client.api.Client;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Flink SinkFunction实现，将UserShortTermData批量写入ClickHouse表user_short_term_data。
 */
@Slf4j
public class ClickHouseSink implements SinkFunction<UserShortTermData> {
    private final String tableName;
    private final Client client;
    private final List<UserShortTermData> buffer = new ArrayList<>();
    private final int batchSize = 10; // 可根据实际情况调整批量大小

    public ClickHouseSink(String tableName, Client client) {
        this.tableName = tableName;
        this.client = client;
    }

    @Override
    public void invoke(UserShortTermData value) {
        try {
            buffer.add(value);
            if (buffer.size() >= batchSize) {
                flush();
            }
        } catch (Exception e) {
            log.error("处理记录时发生错误", e);
        }
    }

    public void close() {
    }

    private void flush() {
        if (buffer.isEmpty()) return;
        try {
            InsertSettings settings = new InsertSettings();
            CompletableFuture<InsertResponse> future = client.insert(tableName, new ArrayList<>(buffer), settings);
            future.get(); // 等待插入完成
            log.info("成功批量写入ClickHouse，表：{}，条数：{}", tableName, buffer.size());
        } catch (Exception e) {
            log.error("写入ClickHouse失败", e);
        } finally {
            buffer.clear();
        }
    }
}
