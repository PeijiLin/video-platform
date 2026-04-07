package com.lpjpro.config;

import com.lpjpro.properties.VideoStorageProperties;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;

/**
 * @author HL
 */
@Configuration
@ConditionalOnProperty(prefix = "video.s3", name = "enabled", havingValue = "true")
public class S3Config {

    @Resource
    private VideoStorageProperties videoStorageProperties;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .endpointOverride(URI.create(videoStorageProperties.getEndpoint()))
            .region(Region.of(videoStorageProperties.getRegion()))
            .forcePathStyle(true)  // MinIO 必须开启路径风格
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    videoStorageProperties.getAccessKey(),
                    videoStorageProperties.getSecretKey()
                )))
                .overrideConfiguration(builder -> builder
                .apiCallTimeout(Duration.ofSeconds(30))
                .apiCallAttemptTimeout(Duration.ofSeconds(60)))
                .httpClientBuilder(ApacheHttpClient.builder()
                                .maxConnections(50)
                                .connectionTimeout(Duration.ofSeconds(5))
                                .socketTimeout(Duration.ofSeconds(30))
                ).build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
            .endpointOverride(URI.create(videoStorageProperties.getEndpoint()))
            .region(Region.of(videoStorageProperties.getRegion()))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    videoStorageProperties.getAccessKey(),
                    videoStorageProperties.getSecretKey()
                )
            ))
            .build();
    }
}