/*
package com.lpjpro.filter;

import com.lpjpro.utils.JSONUtils;
import com.lpjpro.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class TokenGlobalFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();
        List<String> strings = headers.get("Authorization");
        if (strings==null || strings.isEmpty()) {
            return chain.filter(exchange);
        }
        String token = strings.get(0);
        // 处理token
        String substring = token.substring(7);
        Claims claims;
        //        解析token，如果解析失败，返回错误结果（未登录）
        try {
            claims = JwtUtils.parseJwt(substring);
            String json = JSONUtils.toJson(claims);
            ServerHttpRequest newRequest = exchange.getRequest().mutate()
                    .header("X-Token-User", json) // 添加一个新 header
                    .build();

            // 构造新的 exchange
            ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

            // 继续执行过滤链
            return chain.filter(newExchange);
        } catch (Exception e) {
            log.warn("Token无效（请求继续执行）: {}", e.getMessage());
            return chain.filter(exchange); // 解析失败，继续转发原始请求
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
*/
