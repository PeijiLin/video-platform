package com.lpjpro.schedule;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.ContentType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lpjpro.model.ElasticIndex;
import com.lpjpro.model.video.entity.Video;
import com.lpjpro.service.VideoService;
import com.lpjpro.utils.JSONUtils;
import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ElasticSearchSchedule {

    @Resource
    private RestHighLevelClient client;

    @Resource
    private VideoService videoService;


    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Shanghai")
    public void updateDocument() throws IOException {
        int pageSize = 100, pageNum = 0;
        while (true) {
            List<Video> list = videoService.lambdaQuery().list(new Page<>(pageNum, pageSize));
            if (list.isEmpty()) {
                return;
            }
            BulkRequest bulkRequest = new BulkRequest();
            list.forEach(video -> bulkRequest.add(new IndexRequest("videos").source(JSONUtils.toJson(BeanUtil.copyProperties(video, ElasticIndex.class)), XContentType.JSON)));
            pageNum++;
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
        }
    }
}
