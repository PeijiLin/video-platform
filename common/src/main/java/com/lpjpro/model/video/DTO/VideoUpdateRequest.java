package com.lpjpro.model.video.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@Data
public class VideoUpdateRequest {
    private Long videoId;
    /**
     * 发布者ID（外键）
     */
    private Long userId;

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频描述
     */
    private String description;

    /**
     * 封面图URL
     */
    private MultipartFile coverFile;

    /**
     * 视频时长（秒）
     */
    private Integer duration;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标签（逗号分隔，如：#旅行,#美食）
     */
    private String tags;

    /**
     * 上传时间
     */
    private Date createdTime;

    /**
     * 最后更新时间
     */
    private Date updatedTime;
}
