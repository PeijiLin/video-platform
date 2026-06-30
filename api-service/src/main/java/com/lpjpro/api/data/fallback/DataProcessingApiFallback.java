package com.lpjpro.api.data.fallback;

import com.lpjpro.api.data.DataProcessingApi;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataProcessingApiFallback implements FallbackFactory<DataProcessingApi> {

    @Override
    public DataProcessingApi create(Throwable cause) {
        return new DataProcessingApi() {
            @Override
            public BaseResponse<Void> updatePreferenceOnLike(Long userId, Long videoId) {
                log.warn("updatePreferenceOnLike 调用失败, userId={}, videoId={}", userId, videoId, cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }

            @Override
            public BaseResponse<Void> updatePreferenceOnFavorite(Long userId, Long videoId) {
                log.warn("updatePreferenceOnFavorite 调用失败, userId={}, videoId={}", userId, videoId, cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }

            @Override
            public BaseResponse<Void> updatePreferenceOnComment(Long userId, Long videoId) {
                log.warn("updatePreferenceOnComment 调用失败, userId={}, videoId={}", userId, videoId, cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }
        };
    }
}
