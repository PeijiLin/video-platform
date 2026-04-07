package com.lpjpro.controller;

import cn.hutool.core.bean.BeanUtil;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.constant.BaseUserInfo;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.model.user.DTO.UserLoginRequest;
import com.lpjpro.model.user.DTO.UserRegisterRequest;
import com.lpjpro.model.user.VO.UserVO;
import com.lpjpro.model.user.entity.User;
import com.lpjpro.service.UserService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.ResultUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest 注册参数
     * @return 用户 id
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userRegisterRequest), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.userRegister(userRegisterRequest));
    }


    /**
     * 用户登录
     * @param userLoginRequest 登录参数
     * @return jwt
     */
    @PostMapping("/login")
    public BaseResponse<Map<String,Object>> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userLoginRequest),ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.userLogin(userLoginRequest));
    }

    /**
     * 根据id获取当前登录用户信息
     * @return userVO
     */
    @GetMapping("/get")
    public BaseResponse<UserVO> getCurrentUser() {
        Long currentUserId = BaseUserInfo.getCurrentUserId();
        User user = userService.getById(currentUserId);
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 根据id列表批量获取用户id列表
     * @return
     */
    @GetMapping("/getList")
    public BaseResponse<List<UserVO>> getUserByIds(@RequestParam(value = "ids") List<Long> ids) {
        ThrowsUtils.throwIf(CommonHandle.isNull(ids) || ids.isEmpty(), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userService.batchSelect(ids));
    }


    /**
     * 根据id获取用户信息
     * @return userVO
     */
    @GetMapping("/getById")
    public BaseResponse<UserVO> getUserById(@RequestParam(value = "id") Long id) {
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }


    /**
     * 刷新token
     * @param payload
     * @return
     */
    @PostMapping("/refresh-token")
    public BaseResponse<Map<String, String>> refreshToken(@RequestBody Map<String, String> payload) {
        return ResultUtils.success(userService.refreshToken(payload));
    }

    @PostMapping("/logout")
    public BaseResponse<?> logout(@RequestHeader("Authorization") String authHeader) {
        userService.logout(authHeader);
        return ResultUtils.success(null, "登出成功");
    }


}
