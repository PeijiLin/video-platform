package com.lpjpro.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.model.userpicture.entity.UserPicture;
import com.lpjpro.service.PreferenceComputeService;
import com.lpjpro.service.UserPictureService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户画像控制器
 */
@RestController
@RequestMapping("/picture")
public class UserPictureController {

    @Resource
    private UserPictureService userPictureService;

    @Resource
    private PreferenceComputeService preferenceComputeService;

    /**
     * 获取单个用户画像
     */
    @GetMapping("/get")
    public BaseResponse<UserPicture> getUserPicture(@RequestParam("userId") Long userId) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userId) || userId <= 0, ErrorCode.PARAMS_ERROR);
        UserPicture userPicture = userPictureService.getOne(
                new LambdaQueryWrapper<UserPicture>().eq(UserPicture::getUserId, userId));
        return ResultUtils.success(userPicture);
    }

    /**
     * 获取除指定用户外的所有用户画像（用于相似用户计算）
     */
    @GetMapping("/getList")
    public BaseResponse<List<UserPicture>> getUserPictureList(@RequestParam("userId") Long userId) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userId) || userId <= 0, ErrorCode.PARAMS_ERROR);
        List<UserPicture> list = userPictureService.list(
                new LambdaQueryWrapper<UserPicture>().ne(UserPicture::getUserId, userId));
        return ResultUtils.success(list);
    }

    /**
     * 触发用户偏好重算（推荐服务调用）
     */
    @PutMapping("/update")
    public BaseResponse updateUserPicture(@RequestParam("userId") Long userId) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userId), ErrorCode.PARAMS_ERROR);
        preferenceComputeService.computeSingleUserPreference(userId);
        return ResultUtils.success(null, "更新成功");
    }

    /**
     * 点赞行为触发偏好更新（interaction-service Feign 调用）
     */
    @PostMapping("/updateOnLike")
    public BaseResponse<Void> updatePreferenceOnLike(@RequestParam("userId") Long userId,
                                                     @RequestParam("videoId") Long videoId) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userId) || CommonHandle.isNull(videoId), ErrorCode.PARAMS_ERROR);
        preferenceComputeService.updatePreferenceOnBehavior(userId, videoId, "like");
        return ResultUtils.success(null);
    }

    /**
     * 收藏行为触发偏好更新（interaction-service Feign 调用）
     */
    @PostMapping("/updateOnFavorite")
    public BaseResponse<Void> updatePreferenceOnFavorite(@RequestParam("userId") Long userId,
                                                        @RequestParam("videoId") Long videoId) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userId) || CommonHandle.isNull(videoId), ErrorCode.PARAMS_ERROR);
        preferenceComputeService.updatePreferenceOnBehavior(userId, videoId, "favorite");
        return ResultUtils.success(null);
    }

    /**
     * 评论行为触发偏好更新（interaction-service Feign 调用）
     */
    @PostMapping("/updateOnComment")
    public BaseResponse<Void> updatePreferenceOnComment(@RequestParam("userId") Long userId,
                                                        @RequestParam("videoId") Long videoId) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userId) || CommonHandle.isNull(videoId), ErrorCode.PARAMS_ERROR);
        preferenceComputeService.updatePreferenceOnBehavior(userId, videoId, "comment");
        return ResultUtils.success(null);
    }

    /**
     * 获取用户分类偏好得分映射（供推荐服务使用）
     */
    @GetMapping("/scores")
    public BaseResponse<Map<Long, Double>> getUserScores(@RequestParam("userId") Long userId) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userId) || userId <= 0, ErrorCode.PARAMS_ERROR);
        Map<Long, Double> scores = preferenceComputeService.getUserCategoryScores(userId);
        return ResultUtils.success(scores);
    }
}
