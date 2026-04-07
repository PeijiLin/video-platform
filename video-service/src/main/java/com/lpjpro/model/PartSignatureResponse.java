package com.lpjpro.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// PartSignatureResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartSignatureResponse {
    private String uploadUrl;
    private Integer partNumber;
}