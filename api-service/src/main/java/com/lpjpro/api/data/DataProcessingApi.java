package com.lpjpro.api.data;

import com.lpjpro.api.data.fallback.DataProcessingApiFallback;
import com.lpjpro.constant.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "data-processing-service", name = "data-processing-service", fallback = DataProcessingApiFallback.class)
public interface DataProcessingApi {

    @PostMapping("/picture/updateOnLike")
    BaseResponse<Void> updatePreferenceOnLike(@RequestParam("userId") Long userId,
                                               @RequestParam("videoId") Long videoId);

    @PostMapping("/picture/updateOnFavorite")
    BaseResponse<Void> updatePreferenceOnFavorite(@RequestParam("userId") Long userId,
                                                  @RequestParam("videoId") Long videoId);

    @PostMapping("/picture/updateOnComment")
    BaseResponse<Void> updatePreferenceOnComment(@RequestParam("userId") Long userId,
                                                 @RequestParam("videoId") Long videoId);
}
