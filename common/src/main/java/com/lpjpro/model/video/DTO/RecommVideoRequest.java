package com.lpjpro.model.video.DTO;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class RecommVideoRequest implements Serializable {
    private Map<String, Integer> videoSum;
    private List<Long> videoIds;
}
