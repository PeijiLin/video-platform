package com.lpjpro.feign.fallback;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.feign.VideoServiceFeignClient;
import com.lpjpro.model.video.DTO.RecommVideoRequest;
import com.lpjpro.model.video.entity.Video;
import com.lpjpro.utils.ResultUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Component
public class VideoServiceFeignClientFallback implements VideoServiceFeignClient {

    @Override
    public BaseResponse<List<List<Video>>> getList(RecommVideoRequest recommVideoRequest) {
        return null;
    }
}
