package com.lpjpro.model.video.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoMetadata {
    private String filename;
    private String originalName;
    private Long size;
    private String contentType;
    private String duration;         // 时长（秒）
    private String width;            // 宽度
    private String height;           // 高度
    private String codec;            // 编码格式
    private String bitrate;          // 码率
    private String fps;              // 帧率
    private String uploadId;
    private String userId;
    private LocalDateTime uploadTime;
    private String status;           // UPLOADING, PROCESSING, READY, FAILED
    private String coverUrl;
    private Map<String, String> qualities; // 多清晰度URL
}