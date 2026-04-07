package com.lpjpro.model.videolike.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class VideoLikesRequest implements Serializable {

    /**
     * 视频ID，标识被点赞的视频
     */
    @NotNull
    private Long videoId;
}
