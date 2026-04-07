package com.lpjpro.service.impl;

import com.lpjpro.utils.EmailApi;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ThreadService {

    @Resource
    private EmailApi emailApi;

    /**
     * 发送邮箱
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    @Async("taskExecutor")
    public void sendSimpleMail(String subject, String content, String to) {
        emailApi.sendGeneralEmail(subject, content, to);
    }
}