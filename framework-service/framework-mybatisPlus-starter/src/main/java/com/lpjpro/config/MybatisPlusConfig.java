package com.lpjpro.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置分页最大限制，防止恶意请求导致系统风险，具体数值可根据业务调整
        paginationInnerInterceptor.setMaxLimit(500L);
        // 显式开启 Count SQL 优化，提升分页查询性能
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        interceptor.addInnerInterceptor(paginationInnerInterceptor); // 如果配置多个插件，切记分页最后添加
        // 如果有多数据源可以不配具体类型，否则都建议配上具体的 DbType
        return interceptor;
    }
}