package com.lpjpro.api.user;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.model.user.VO.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "user-service", name = "user-service")
public interface UserApi {

    @GetMapping("/user/getList")
    BaseResponse<List<UserVO>> getUserByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/user/getById")
    BaseResponse<UserVO> getUserById(@RequestParam("id") Long id);
}