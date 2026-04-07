package com.lpjpro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lpjpro.model.videofavorites.entity.VideoFavorites;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author HL
* @description 针对表【video_favorites(记录用户对视频的收藏行为)】的数据库操作Mapper
* @createDate 2025-04-21 12:48:01
* @Entity generator.domain.VideoFavorites
*/
public interface VideoFavoritesMapper extends BaseMapper<VideoFavorites> {
    List<Boolean> batchFavorites(List<Long> videoIds, Long userId);
}




