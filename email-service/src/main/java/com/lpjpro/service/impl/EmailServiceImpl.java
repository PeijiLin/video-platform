package com.lpjpro.service.impl;

import cn.hutool.core.lang.UUID;
import com.alibaba.nacos.api.utils.StringUtils;
import com.lpjpro.constant.RedisConstant;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.model.user.DTO.UserEmailRequest;
import com.lpjpro.service.EmailService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.StringUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class EmailServiceImpl implements EmailService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ThreadService threadService;

    @Override
    public String getRequestPermissionCode(String email) {
        // 非空校验
        ThrowsUtils.throwIf(StringUtils.isBlank(email), ErrorCode.PARAMS_ERROR);

        // 邮箱校验
        ThrowsUtils.throwIf(!StringUtil.isValidEmail(email), ErrorCode.PARAMS_ERROR, "邮箱格式有误");

        // 随机生成权限码
        String permissionCode = UUID.randomUUID().toString();

        // 存入redis，缓存10s
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(RedisConstant.EMAIL_REQUEST_VERIFY + email, permissionCode, 60, TimeUnit.SECONDS);

        return permissionCode;
    }

    @Override
    public void sendEmailCode(UserEmailRequest userEmailRequest) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userEmailRequest), ErrorCode.PARAMS_ERROR);
        // 获取权限码和邮箱
        String email = userEmailRequest.getEmail();
        String permissionCode = userEmailRequest.getCode();

        // 邮箱校验
        ThrowsUtils.throwIf(!StringUtil.isValidEmail(email), ErrorCode.PARAMS_ERROR, "邮箱格式有误");
        // 权限码比对
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String rightCode = (String) valueOperations.get(RedisConstant.EMAIL_REQUEST_VERIFY + email);

        ThrowsUtils.throwIf(!permissionCode.equals(rightCode), ErrorCode.FORBIDDEN_ERROR);
        redisTemplate.delete(RedisConstant.EMAIL_REQUEST_VERIFY + email);

        // 全部通过
        // 随机生成6位数字验证码
        String code = StringUtil.randomSixCode();

        // 正文内容
        String content = "亲爱的用户：\n" +
                "您此次的验证码为：\n\n" +
                code + "\n\n" +
                "此验证码5分钟内有效，请立即进行下一步操作。 如非你本人操作，请忽略此邮件。\n" +
                "感谢您的使用！";

        // 发送验证码
        threadService.sendSimpleMail("您此次的验证码为：" + code, content, email);
        // 丢入缓存，设置5分钟过期
        valueOperations.set(RedisConstant.EMAIL + email, code, 5, TimeUnit.MINUTES);
    }
}
