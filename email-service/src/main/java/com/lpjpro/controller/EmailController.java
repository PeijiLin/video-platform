package com.lpjpro.controller;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.model.user.DTO.UserEmailRequest;
import com.lpjpro.service.EmailService;
import com.lpjpro.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Resource
    private EmailService emailService;

    @GetMapping("code/request")
    public BaseResponse<String> getRequestPermissionCode(@RequestParam(value = "email") String email) {
        return ResultUtils.success(emailService.getRequestPermissionCode(email));
    }

    // 邮箱验证码接口
    @PostMapping("code/email")
    public BaseResponse<String> sendEmailCode(@RequestBody UserEmailRequest userEmailRequest) {
        emailService.sendEmailCode(userEmailRequest);
        return ResultUtils.success(null, "发送成功");
    }
}
