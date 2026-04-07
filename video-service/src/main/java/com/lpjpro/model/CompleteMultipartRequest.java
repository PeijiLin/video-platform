package com.lpjpro.model;

import com.lpjpro.model.s3.PartInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// CompleteMultipartRequest.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMultipartRequest {
    @NotBlank
    private String uploadId;
    @NotBlank
    private String objectKey;
    @NotEmpty
    private List<PartInfo> parts;
}