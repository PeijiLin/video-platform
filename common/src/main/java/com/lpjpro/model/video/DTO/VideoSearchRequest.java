package com.lpjpro.model.video.DTO;

import com.lpjpro.constant.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class VideoSearchRequest extends PageRequest {
    private String keyword;
}
