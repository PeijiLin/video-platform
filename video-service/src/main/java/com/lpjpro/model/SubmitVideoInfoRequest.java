package com.lpjpro.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitVideoInfoRequest {
    @NotNull
    private Long videoId;
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private Long categoryId;
    private String tags;
    private MultipartFile coverFile;
}