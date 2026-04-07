package com.lpjpro.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lpjpro.model.videolike.DTO.VideoLikesRequest;
import com.lpjpro.model.videolike.entity.VideoLikes;

import java.util.List;

/**
* @author HL
* @description 针对表【video_likes(记录用户对视频的点赞行为)】的数据库操作Service
* @createDate 2025-04-21 12:48:14
*/
public interface VideoLikesService extends IService<VideoLikes> {
    /**
     * 点赞
     * @param videoLikesRequest 视频和用户id
     * @return 点赞id
     */
    Boolean upvote(VideoLikesRequest videoLikesRequest);

    /**
     * 取消点赞
     * @param videoLikesRequest
     * @return
     */
    Boolean unUpvote(VideoLikesRequest videoLikesRequest);

    /**
     * 批量查询用户是否点赞
     * @param videoIds
     * @param userId
     * @return
     */
    List<Boolean> batchLiked(List<Long> videoIds, Long userId);
}
