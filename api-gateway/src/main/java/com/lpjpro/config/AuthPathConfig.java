package com.lpjpro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@ConfigurationProperties(prefix = "auth")
public class AuthPathConfig {
    private List<String> publicPaths;
    private List<String> optionalAuthPaths;
}