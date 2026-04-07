package com.lpjpro.api.video;

import com.lpjpro.api.video.fallback.VideoApiFallback;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.model.video.DTO.RecommVideoRequest;
import com.lpjpro.model.video.VO.GetVideoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "video-service", name = "video-service", fallback = VideoApiFallback.class)
public interface VideoApi {

    @GetMapping("/video/get")
    BaseResponse<GetVideoVO> getById(@RequestParam("id") Long id);

    @PostMapping("/video/getList")
    BaseResponse<List<List>> getList(@RequestBody RecommVideoRequest recommVideoRequest);

    @GetMapping("/video/category/list")
    BaseResponse<List> getCategoryList();

    @GetMapping("/video/getById")
    BaseResponse<GetVideoVO> getVideoById(@RequestParam("id") Long id);

    @PutMapping("/video/update/favorites")
    BaseResponse updateVideoFavorites(@RequestParam(value = "id") Long id, @RequestParam(value = "count") Long count);

    @PutMapping("/video/update/likes")
    BaseResponse updateVideoLikes(@RequestParam(value = "id") Long id, @RequestParam(value = "count") Long count);
}