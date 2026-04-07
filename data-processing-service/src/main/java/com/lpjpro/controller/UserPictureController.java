package com.lpjpro.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.model.userpicture.entity.UserPicture;
import com.lpjpro.model.video.entity.Video;
import com.lpjpro.service.FeatureService;
import com.lpjpro.service.UserPictureService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/picture")
public class UserPictureController {

    @Resource
    private UserPictureService userPictureService;

    @Resource
    private FeatureService featureService;

    @GetMapping("/get")
    public BaseResponse<UserPicture> getUserPicture(@RequestParam(value = "userId") Long userId) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userId) || userId <= 0, ErrorCode.PARAMS_ERROR);
        UserPicture userPicture = userPictureService.getOne(new LambdaQueryWrapper<UserPicture>().eq(UserPicture::getUserId, userId));
        return ResultUtils.success(userPicture);
    }

    /**
     * 获取除对象id外的所有数据
     * @param userId
     * @return
     */
    @GetMapping("/getList")
    public BaseResponse<List<UserPicture>> getUserPictureList(@RequestParam(value = "userId") Long userId) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userId) || userId <= 0, ErrorCode.PARAMS_ERROR);
        List<UserPicture> list = userPictureService.list(new LambdaQueryWrapper<UserPicture>().ne(UserPicture::getUserId, userId));
        return ResultUtils.success(list);
    }

    @PutMapping("/update")
    public BaseResponse updateUserPicture(@RequestParam(value = "userId") Long userId) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userId), ErrorCode.PARAMS_ERROR);
        featureService.updateUserFeature(userId);
        return ResultUtils.success(null, "更新成功");
    }


}
