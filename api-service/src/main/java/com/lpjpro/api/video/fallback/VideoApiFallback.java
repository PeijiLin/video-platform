package com.lpjpro.api.video.fallback;

import com.lpjpro.api.video.VideoApi;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.model.video.DTO.RecommVideoRequest;
import com.lpjpro.model.video.VO.GetVideoVO;
import com.lpjpro.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class VideoApiFallback implements FallbackFactory<VideoApi> {

    @Override
    public VideoApi create(Throwable cause) {
        return new VideoApi() {
            @Override
            public BaseResponse<GetVideoVO> getById(Long id) {
                log.error("VideoApi.getById 调用失败: {}", id, cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }

            @Override
            public BaseResponse<List<List>> getList(RecommVideoRequest recommVideoRequest) {
                log.error("VideoApi.getList 调用失败", cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }

            @Override
            public BaseResponse<List> getCategoryList() {
                log.error("VideoApi.getCategoryList 调用失败", cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }

            @Override
            public BaseResponse<GetVideoVO> getVideoById(Long id) {
                log.error("VideoApi.getVideoById 调用失败: {}", id, cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }

            @Override
            public BaseResponse updateVideoFavorites(Long id, Long count) {
                log.error("VideoApi.updateVideoFavorites 调用失败: {}", id, cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }

            @Override
            public BaseResponse updateVideoLikes(Long id, Long count) {
                log.error("VideoApi.updateVideoLikes 调用失败: {}", id, cause);
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "服务降级");
            }
        };
    }
}