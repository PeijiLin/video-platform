package com.lpjpro.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lpjpro.model.user.DTO.UserLoginRequest;
import com.lpjpro.model.user.DTO.UserRegisterRequest;
import com.lpjpro.model.user.VO.UserVO;
import com.lpjpro.model.user.entity.User;

import java.util.List;
import java.util.Map;

/**
* @author HL
* @description 针对表【user(用户信息表)】的数据库操作Service
* @createDate 2025-03-13 16:58:05
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userRegisterRequest 注册参数
     * @return 用户 id
     */
    Long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     * @param userLoginRequest 登录参数
     * @return jwt
     */
    Map<String,Object> userLogin(UserLoginRequest userLoginRequest);

    /**
     * 批量查询
     * @param ids
     * @return
     */
    List<UserVO> batchSelect(List<Long> ids);

    /**
     * 刷新token
     * @param payload
     * @return
     */
    Map<String, String> refreshToken(Map<String, String> payload);

    /**
     * 登出
     */
    void logout(String authHeader);
}
