package com.lpjpro.config;

import com.lpjpro.interceptor.AuthInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebInterceptorConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 设置所有的路径都要进行拦截，除了/test/login
                /*.excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        //Knife4j/Swagger核心路径
                        "/doc.html",
                        // Knife4j前端页面
                        "/webjars/**",
                        //静态资源(JS/CSS)
                        "/swagger-resources/**",// Swagger配置资源
                        "/v3/api-docs",
                        // 0penAPI JSON描述(Swagger 2)
                        //0penAPI JSON描述(Swagger 3
                        "/v3/api-docs/**",
                        "/favicon.ico");*/
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
    }

}
