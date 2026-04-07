package com.lpjpro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lpjpro.model.videolike.entity.VideoLikes;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author HL
* @description 针对表【video_likes(记录用户对视频的点赞行为)】的数据库操作Mapper
* @createDate 2025-04-21 12:48:14
* @Entity generator.domain.VideoLikes
*/
public interface VideoLikesMapper extends BaseMapper<VideoLikes> {

    List<Boolean> batchLiked(List<Long> videoIds, Long userId);
}




