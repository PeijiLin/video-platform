package com.lpjpro.feign.fallback;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.feign.UserPictureServiceFeignClient;
import com.lpjpro.model.userpicture.entity.UserPicture;
import com.lpjpro.utils.ResultUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserPictureServiceFeignClientFallback implements UserPictureServiceFeignClient {


    @Override
    public BaseResponse<UserPicture> getUserPicture(@RequestParam(value = "userId") Long userId) {
        return ResultUtils.success(new UserPicture());
    }

    @Override
    public BaseResponse<List<UserPicture>> getUserPictureList(@RequestParam(value = "userId") Long userId) {
        return ResultUtils.success(new ArrayList<>());
    }

    @Override
    public BaseResponse updateUserPicture(Long userId) {
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}
