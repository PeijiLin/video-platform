package com.lpjpro.model.videofavorites.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;

@Data
public class VideoFavoritesRequest implements Serializable {

    /**
     * 视频ID，标识被收藏的视频
     */
    @NotNull
    private Long videoId;
}
