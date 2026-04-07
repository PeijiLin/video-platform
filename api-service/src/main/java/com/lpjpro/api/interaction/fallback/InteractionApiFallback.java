package com.lpjpro.api.interaction.fallback;

import com.lpjpro.api.interaction.InteractionApi;
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
public class InteractionApiFallback implements FallbackFactory<InteractionApi> {

    @Override
    public InteractionApi create(Throwable cause) {
        return new InteractionApi() {
            @Override
            public BaseResponse<List<Boolean>> batchFavorites(List<Long> videoIds, Long userId) {
                log.error("InteractionApi.batchFavorites 调用失败", cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }

            @Override
            public BaseResponse<List<Boolean>> batchLikes(List<Long> videoIds, Long userId) {
                log.error("InteractionApi.batchLikes 调用失败", cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }
        };
    }
}