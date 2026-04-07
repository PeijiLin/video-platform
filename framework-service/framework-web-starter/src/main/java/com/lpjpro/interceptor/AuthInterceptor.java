package com.lpjpro.interceptor;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.lpjpro.constant.BaseContext;
import com.lpjpro.constant.BaseUserInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Value("${jwt.sign-key:hzy123}")
    private String signKey;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader("X-User-ID");

        if (StringUtils.isBlank(userId)) {
            BaseUserInfo.clear();
            return true;
        }

        BaseUserInfo.set(BaseContext.ID, userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseUserInfo.clear();
    }
}