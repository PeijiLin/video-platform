package com.lpjpro.controller;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.model.videofavorites.DTO.VideoFavoritesRequest;
import com.lpjpro.service.VideoFavoritesService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@ResponseBody
@RequestMapping("/video/favorites")
public class VideoFavoritesController {

    @Resource
    private VideoFavoritesService videoFavoritesService;

    /**
     * 收藏
     * @param videoFavoritesRequest 视频id
     * @return 收藏id
     */
    @PostMapping("/add")
    public BaseResponse<Long> collect(@RequestBody VideoFavoritesRequest videoFavoritesRequest) {
        ThrowsUtils.throwIf(CommonHandle.isNull(videoFavoritesRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(videoFavoritesService.collect(videoFavoritesRequest));
    }


    /**
     * 取消收藏
     * @param videoFavoritesRequest 视频id
     * @return 收藏id
     */
    @PostMapping("/remove")
    public BaseResponse<Boolean> unCollect(@RequestBody VideoFavoritesRequest videoFavoritesRequest) {
        ThrowsUtils.throwIf(CommonHandle.isNull(videoFavoritesRequest),ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(videoFavoritesService.unCollect(videoFavoritesRequest));
    }

    /**
     * 批量查询用户是否收藏
     * @return
     */
    @GetMapping("/getFavoritesList")
    public BaseResponse<List<Boolean>> batchFavorites(@RequestParam(value = "videoIds") List<Long> videoIds, @RequestParam(value = "userId") Long userId) {
        ThrowsUtils.throwIf(videoIds.isEmpty(),ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(videoFavoritesService.batchFavorites(videoIds, userId));
    }
}
