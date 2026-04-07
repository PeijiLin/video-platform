package com.lpjpro.controller;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.model.videolike.DTO.VideoLikesRequest;
import com.lpjpro.service.VideoLikesService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 点赞相关接口
 */
@RestController
@RequestMapping("/video/likes")
public class VideoLikesController {

    @Resource
    private VideoLikesService videoLikesService;

    /**
     * 点赞
     * @param videoLikesRequest 视频和用户id
     * @return 点赞id
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> upvote(@RequestBody VideoLikesRequest videoLikesRequest) {
        ThrowsUtils.throwIf(CommonHandle.isNull(videoLikesRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(videoLikesService.upvote(videoLikesRequest));
    }

    /**
     * 取消点赞
     * @param videoLikesRequest 视频id
     * @return void
     */
    @PostMapping("/remove")
    public BaseResponse<Boolean> unUpvote(@RequestBody VideoLikesRequest videoLikesRequest) {
        ThrowsUtils.throwIf(CommonHandle.isNull(videoLikesRequest),ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(videoLikesService.unUpvote(videoLikesRequest));
    }

    /**
     * 批量查询用户是否点赞
     * @return
     */
    @GetMapping("/getLikesList")
    public BaseResponse<List<Boolean>> batchLikes(@RequestParam(value = "videoIds") List<Long> videoIds, @RequestParam(value = "userId") Long userId) {
        ThrowsUtils.throwIf(videoIds.isEmpty(),ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(videoLikesService.batchLiked(videoIds, userId));
    }

}
