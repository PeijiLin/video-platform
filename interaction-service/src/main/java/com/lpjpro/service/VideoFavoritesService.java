package com.lpjpro.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lpjpro.model.videofavorites.DTO.VideoFavoritesRequest;
import com.lpjpro.model.videofavorites.entity.VideoFavorites;

import java.util.List;

/**
* @author HL
* @description 针对表【video_favorites(记录用户对视频的收藏行为)】的数据库操作Service
* @createDate 2025-04-21 12:48:01
*/
public interface VideoFavoritesService extends IService<VideoFavorites> {

    /**
     * 收藏
     * @param videoFavoritesRequest 视频和用户id
     * @return 收藏id
     */
    Long collect(VideoFavoritesRequest videoFavoritesRequest);

    /**
     * 取消收藏
     * @param videoFavoritesRequest 视频id
     * @return 收藏id
     */
    Boolean unCollect(VideoFavoritesRequest videoFavoritesRequest);

    /**
     * 批量查询用户是否收藏
     * @return
     */
    List<Boolean> batchFavorites(List<Long> videoIds, Long userId);
}
