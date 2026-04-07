package com.lpjpro.api.user.fallback;

import com.lpjpro.api.user.UserApi;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.model.user.VO.UserVO;
import com.lpjpro.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserApiFallback implements FallbackFactory<UserApi> {

    @Override
    public UserApi create(Throwable cause) {
        return new UserApi() {
            @Override
            public BaseResponse<List<UserVO>> getUserByIds(List<Long> ids) {
                log.error("userApi.getUserByIds 调用失败", cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }

            @Override
            public BaseResponse<UserVO> getUserById(Long id) {
                log.error("userApi.getUserById 调用失败", cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }
        };
    }
}
