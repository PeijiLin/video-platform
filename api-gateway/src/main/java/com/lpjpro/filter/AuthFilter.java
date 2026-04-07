package com.lpjpro.filter;

import com.lpjpro.config.AuthPathConfig;
import com.lpjpro.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.sign-key:hzy123}")
    private String signKey;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private AuthPathConfig authPathConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 白名单路径：无需 Token，直接放行
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // 可选 Token 路径：尝试解析 Token，但不强制拦截
        if (isOptionalAuthPath(path)) {
            String token = extractToken(request);
            if (token != null && isValidToken(token)) {
                String userId = JwtUtils.getUserIdFromToken(token, signKey);
                ServerHttpRequest newRequest = request.mutate()
                        .header("X-User-ID", userId)
                        .build();
                return chain.filter(exchange.mutate().request(newRequest).build());
            }
        }

        // 默认路径：必须携带 Token
        String token = extractToken(request);
        if (token == null || !isValidToken(token)) {
            return unauthorized(exchange, "Missing or invalid token");
        }

        String userId = JwtUtils.getUserIdFromToken(token, signKey);
        ServerHttpRequest newRequest = request.mutate()
                .header("X-User-ID", userId)
                .build();

        return chain.filter(exchange.mutate().request(newRequest).build());
    }

    private boolean isPublicPath(String path) {
        List<String> publicPaths = authPathConfig.getPublicPaths();
        System.out.println("--- " + publicPaths);
        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOptionalAuthPath(String path) {
        return authPathConfig.getOptionalAuthPaths().stream()
                .anyMatch(path::startsWith);
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isValidToken(String token) {
        try {
            String blacklistKey = "blacklist:access_token:" + token;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                return false;
            }
            JwtUtils.parseJwt(token, signKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}