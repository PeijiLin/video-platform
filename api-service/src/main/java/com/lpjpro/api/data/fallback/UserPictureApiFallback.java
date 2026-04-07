package com.lpjpro.api.data.fallback;

import com.lpjpro.api.data.UserPictureApi;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class UserPictureApiFallback implements FallbackFactory<UserPictureApi> {

    @Override
    public UserPictureApi create(Throwable cause) {
        return new UserPictureApi() {
            @Override
            public BaseResponse<List> getUserPictureList(Long userId) {
                log.error("UserPictureApi.getUserPictureList 调用失败: {}", userId, cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }
        };
    }
}