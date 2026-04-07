package com.lpjpro.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpjpro.api.user.UserApi;
import com.lpjpro.api.video.VideoApi;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.constant.BaseUserInfo;
import com.lpjpro.exception.BusinessException;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.mapper.VideoFavoritesMapper;
import com.lpjpro.model.user.VO.UserVO;
import com.lpjpro.model.video.VO.GetVideoVO;
import com.lpjpro.model.videofavorites.DTO.VideoFavoritesRequest;
import com.lpjpro.model.videofavorites.entity.VideoFavorites;
import com.lpjpro.service.VideoFavoritesService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author HL
* @description 针对表【video_favorites(记录用户对视频的收藏行为)】的数据库操作Service实现
* @createDate 2025-04-21 12:48:01
*/
@Service
public class VideoFavoritesServiceImpl extends ServiceImpl<VideoFavoritesMapper, VideoFavorites>
    implements VideoFavoritesService {

    @Resource
    private UserApi userServiceFeignClient;

    @Resource
    private VideoApi videoServiceFeignClient;

    @Resource
    private VideoFavoritesMapper videoFavoritesMapper;

    /**
     * 收藏
     * @param videoFavoritesRequest 视频和用户id
     * @return 收藏id
     */
    @Override
    @Transactional
    public Long collect(VideoFavoritesRequest videoFavoritesRequest) {
        Long currentUserId = BaseUserInfo.getCurrentUserId();

        // 校验视频和用户id是否存在
        extracted(videoFavoritesRequest, currentUserId);

        // 记录点赞
        VideoFavorites videoFavorites = new VideoFavorites();
        videoFavorites.setUserId(currentUserId);
        videoFavorites.setVideoId(videoFavoritesRequest.getVideoId());
        videoFavorites.setIsActive(1);

        boolean save = this.save(videoFavorites);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        updateFavorites(videoFavoritesRequest);
        return videoFavorites.getId();
    }



    /**
     * 取消收藏
     * @param videoFavoritesRequest 视频id
     * @return 收藏id
     */
    @Override
    @Transactional
    public Boolean unCollect(VideoFavoritesRequest videoFavoritesRequest) {
        Long currentUserId = BaseUserInfo.getCurrentUserId();
        // 校验视频和用户id是否存在
        extracted(videoFavoritesRequest, currentUserId);
        LambdaQueryWrapper<VideoFavorites> videoFavoritesLambdaQueryWrapper = new LambdaQueryWrapper<>();
        videoFavoritesLambdaQueryWrapper.eq(VideoFavorites::getVideoId,videoFavoritesRequest.getVideoId()).eq(VideoFavorites::getUserId,currentUserId);
        boolean result =  this.remove(videoFavoritesLambdaQueryWrapper);
        updateFavorites(videoFavoritesRequest);
        return result;
    }

    @Override
    public List<Boolean> batchFavorites(List<Long> videoIds, Long userId) {
        return videoFavoritesMapper.batchFavorites(videoIds, userId);
    }

    /**
     * 更新收藏数
     * @param videoFavoritesRequest
     */
    private void updateFavorites(VideoFavoritesRequest videoFavoritesRequest) {
        // 更新收藏数
        LambdaQueryWrapper<VideoFavorites> videoFavoritesLambdaQueryWrapper = new LambdaQueryWrapper<>();
        videoFavoritesLambdaQueryWrapper.eq(VideoFavorites::getVideoId, videoFavoritesRequest.getVideoId());
        long count = this.count(videoFavoritesLambdaQueryWrapper);

        videoServiceFeignClient.updateVideoFavorites(videoFavoritesRequest.getVideoId(), count);
    }

    /**
     * 参数校验
     * @param videoFavoritesRequest
     * @param currentUserId
     */
    private void extracted(VideoFavoritesRequest videoFavoritesRequest, Long currentUserId) {
        BaseResponse<UserVO> userById = userServiceFeignClient.getUserById(currentUserId);
        UserVO user = userById.getData();
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        BaseResponse<GetVideoVO> byId = videoServiceFeignClient.getById(videoFavoritesRequest.getVideoId());
        GetVideoVO video = byId.getData();
        if (video == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
    }
}




