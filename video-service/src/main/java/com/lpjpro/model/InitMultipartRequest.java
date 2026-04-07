package com.lpjpro.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitMultipartRequest {
    @NotBlank(message = "文件名不能为空")
    private String fileName;
    @NotBlank(message = "文件哈希不能为空")
    private String fileHash;
    
    @NotNull(message = "文件大小不能为空")
    private Long fileSize;
    @NotBlank(message = "内容类型不能为空")
    private String contentType;
    /**
     * 分片数
     */
    private Integer totalChunks;

    /**
     * 分片大小
     */
    private Integer chunkSize;
}
