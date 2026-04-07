package com.lpjpro.interceptor;

import com.lpjpro.constant.BaseContext;
import com.lpjpro.constant.BaseUserInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.lpjpro.constant.RedisConstant.USER_LOGIN;

@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tokenId = request.getHeader("Authorization");
        if (StringUtils.isBlank(tokenId) || StringUtils.isEmpty(tokenId)) {
            BaseUserInfo.clear(); // 请求结束后强制清理
            return true;
        }
        tokenId = tokenId.substring(7);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Integer i = (Integer) valueOperations.get(USER_LOGIN + tokenId);
        if (i == null) {
            BaseUserInfo.clear(); // 请求结束后强制清理
        }
        Long userId = Long.valueOf(i);
        BaseUserInfo.set(BaseContext.ID, userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseUserInfo.clear(); // 请求结束后强制清理
    }
}