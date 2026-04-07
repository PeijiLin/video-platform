package com.lpjpro.api.data;

import com.lpjpro.api.data.fallback.UserPictureApiFallback;
import com.lpjpro.constant.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "data-processing-service", name = "data-processing-service", fallback = UserPictureApiFallback.class)
public interface UserPictureApi {

    @GetMapping("/user/picture/list")
    BaseResponse<List> getUserPictureList(@RequestParam("userId") Long userId);
}