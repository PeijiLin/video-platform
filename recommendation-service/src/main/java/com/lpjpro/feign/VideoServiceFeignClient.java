package com.lpjpro.feign;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.feign.fallback.VideoServiceFeignClientFallback;
import com.lpjpro.model.video.DTO.RecommVideoRequest;
import com.lpjpro.model.video.entity.Video;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "video-service", fallback = VideoServiceFeignClientFallback.class)
public interface VideoServiceFeignClient {
    @PostMapping("/video/getList")
    BaseResponse<List<List<Video>>> getList(@RequestBody RecommVideoRequest recommVideoRequest);
}
