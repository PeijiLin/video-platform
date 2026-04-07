package com.lpjpro.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpjpro.api.user.UserApi;
import com.lpjpro.api.video.VideoApi;
import com.lpjpro.config.KafkaConfig;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.constant.BaseUserInfo;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.mapper.VideoLikesMapper;
import com.lpjpro.model.user.VO.UserVO;
import com.lpjpro.model.video.VO.GetVideoVO;
import com.lpjpro.model.videolike.DTO.VideoLikesRequest;
import com.lpjpro.model.videolike.entity.VideoLikes;
import com.lpjpro.pojo.BaseVideoLiked;
import com.lpjpro.service.VideoLikesService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.JSONUtils;
import jakarta.annotation.Resource;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static com.lpjpro.constant.RedisConstant.USER_LIKED;


/**
* @author HL
* @description 针对表【video_likes(记录用户对视频的点赞行为)】的数据库操作Service实现
* @createDate 2025-04-21 12:48:14
*/
@Service
public class VideoLikesServiceImpl extends ServiceImpl<VideoLikesMapper, VideoLikes>
    implements VideoLikesService {

    @Resource
    private UserApi userServiceFeignClient;

    @Resource
    private VideoApi videoServiceFeignClient;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private VideoLikesMapper videoLikesMapper;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;


    /**
     * 点赞
     * @param videoLikesRequest 视频和用户id
     * @return 点赞id
     */
    @Transactional
    @Override
    public Boolean upvote(VideoLikesRequest videoLikesRequest) {
        Long currentUserId = BaseUserInfo.getCurrentUserId();
        extracted(videoLikesRequest, currentUserId);
        String key = USER_LIKED + currentUserId;
        RSet<Long> set = redissonClient.getSet(key);
        set.add(videoLikesRequest.getVideoId());

        BaseVideoLiked baseVideoLiked = new BaseVideoLiked();
        baseVideoLiked.setUserId(currentUserId);
        baseVideoLiked.setVideoId(videoLikesRequest.getVideoId());
        baseVideoLiked.setLikedTime(new Date());
        baseVideoLiked.setIsActive(1);
        String json = JSONUtils.toJson(baseVideoLiked);
        kafkaTemplate.send(KafkaConfig.TOPIC_VIDEO_LIKES, String.valueOf(currentUserId), json);
        return true;
    }


    /**
     * 取消点赞
     * @param videoLikesRequest
     * @return
     */
    @Transactional
    @Override
    public Boolean unUpvote(VideoLikesRequest videoLikesRequest) {
        Long currentUserId = BaseUserInfo.getCurrentUserId();
        extracted(videoLikesRequest, currentUserId);
        String key = USER_LIKED + currentUserId;
        RSet<Long> set = redissonClient.getSet(key);
        set.remove(videoLikesRequest.getVideoId());

        BaseVideoLiked baseVideoLiked = new BaseVideoLiked();
        baseVideoLiked.setUserId(currentUserId);
        baseVideoLiked.setVideoId(videoLikesRequest.getVideoId());
        String json = JSONUtils.toJson(baseVideoLiked);
        kafkaTemplate.send(KafkaConfig.TOPIC_VIDEO_LIKES, String.valueOf(currentUserId), json);
        return true;
    }

    @Override
    public List<Boolean> batchLiked(List<Long> videoIds, Long userId) {
        return videoLikesMapper.batchLiked(videoIds, userId);
    }


    /**
     * 更新视频点赞数
     * @param videoLikesRequest
     */
    private void updateLikes(VideoLikesRequest videoLikesRequest) {
        LambdaQueryWrapper<VideoLikes> videoLikesLambdaQueryWrapper = new LambdaQueryWrapper<>();
        videoLikesLambdaQueryWrapper.eq(VideoLikes::getVideoId, videoLikesRequest.getVideoId());
        long count = this.count(videoLikesLambdaQueryWrapper);
        videoServiceFeignClient.updateVideoLikes(videoLikesRequest.getVideoId(), count);
    }


    /**
     * 参数校验
     * @param videoLikesRequest
     * @param currentUserId
     */
    private void extracted(VideoLikesRequest videoLikesRequest, Long currentUserId) {
        BaseResponse<UserVO> userById = userServiceFeignClient.getUserById(currentUserId);
        UserVO user = userById.getData();
        ThrowsUtils.throwIf(CommonHandle.isNull(user),ErrorCode.NOT_FOUND_ERROR);

        BaseResponse<GetVideoVO> byId = videoServiceFeignClient.getById(videoLikesRequest.getVideoId());
        GetVideoVO video = byId.getData();
        ThrowsUtils.throwIf(CommonHandle.isNull(video),ErrorCode.NOT_FOUND_ERROR);
    }
}




