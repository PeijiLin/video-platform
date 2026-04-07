//package com.lpjpro.filter;
//
//import com.lpjpro.exception.BusinessException;
//import com.lpjpro.exception.ErrorCode;
//import com.lpjpro.utils.JSONUtils;
//import com.lpjpro.utils.JwtUtils;
//import io.jsonwebtoken.Claims;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.api.RBucket;
//import org.redisson.api.RedissonClient;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//import java.util.Objects;
//
//import static com.lpjpro.constant.RedisConstant.USER_LOGIN;
//
//@Slf4j
//@Component
//public class OnceTokenGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {
//
//    @Resource
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Override
//    public GatewayFilter apply(NameValueConfig config) {
//        return new GatewayFilter() {
//            @Override
//            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//                ServerHttpRequest request = exchange.getRequest();
//                String uri = request.getURI().toString();
//                if (uri.contains("get")) {
//                    HttpHeaders headers = request.getHeaders();
//                    List<String> strings = headers.get("Authorization");
//                    if (strings==null || strings.isEmpty()) {
//                        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
//                    }
//                    String tokenId = strings.get(0);
//                    tokenId = tokenId.substring(7);
//
//                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
//                    Object obj = valueOperations.get(USER_LOGIN + tokenId);
//                    Integer userId = (Integer) obj;
//                    if (userId == null || userId <= 0) {
//                        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
//                    }
//                }
//                return chain.filter(exchange);
//            }
//        };
//    }
//}
