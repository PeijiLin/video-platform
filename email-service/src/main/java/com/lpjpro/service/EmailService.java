package com.lpjpro.service;

import com.lpjpro.model.user.DTO.UserEmailRequest;

public interface EmailService {

    /**
     * 获取请求权限码
     * @param email 邮箱
     * @return
     */
    String getRequestPermissionCode(String email);

    /**
     * 发送邮箱验证码
     * @param userEmailRequest （邮箱和权限码）
     */
    void sendEmailCode(UserEmailRequest userEmailRequest);
}
