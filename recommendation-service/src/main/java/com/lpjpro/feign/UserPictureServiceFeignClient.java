package com.lpjpro.feign;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.feign.fallback.UserPictureServiceFeignClientFallback;
import com.lpjpro.model.userpicture.entity.UserPicture;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(value = "data-processing-service", fallback = UserPictureServiceFeignClientFallback.class)
public interface UserPictureServiceFeignClient {
    @GetMapping("/picture/get")
    BaseResponse<UserPicture> getUserPicture(@RequestParam(value = "userId") Long userId);

    @GetMapping("/picture/getList")
    BaseResponse<List<UserPicture>> getUserPictureList(@RequestParam(value = "userId") Long userId);

    @PutMapping("/picture/update")
    BaseResponse updateUserPicture(@RequestParam(value = "userId") Long userId);
}
