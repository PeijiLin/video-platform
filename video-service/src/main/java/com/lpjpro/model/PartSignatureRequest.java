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
public class PartSignatureRequest {
    @NotBlank
    private String uploadId;
    @NotBlank
    private String objectKey;
    @NotNull
    private Integer partNumber;
}