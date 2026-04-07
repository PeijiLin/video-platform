package com.lpjpro.kafka;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lpjpro.api.video.VideoApi;
import com.lpjpro.config.KafkaConfig;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.model.videolike.entity.VideoLikes;
import com.lpjpro.pojo.BaseVideoLiked;
import com.lpjpro.service.VideoLikesService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.JSONUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.lpjpro.constant.RedisConstant.VIDEO_LIKES_LOCK;

@Slf4j
@Component
public class VideoLikesKafkaConsumer {

    private static final long LOCK_WAIT_TIME = 3;
    private static final long LOCK_LEASE_TIME = 10;

    @Resource
    private VideoLikesService videoLikesService;

    @Resource
    private VideoApi videoServiceFeignClient;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Resource
    private RedissonClient redissonClient;

    @KafkaListener(
            topics = KafkaConfig.TOPIC_VIDEO_LIKES,
            groupId = "video-likes-group",
            concurrency = "3"
    )
    public void handleVideoLikes(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String message = record.value();
        String eventType = record.headers().lastHeader("eventType") != null
                ? new String(record.headers().lastHeader("eventType").value())
                : KafkaConfig.EVENT_TYPE_UPVOTE;

        try {
            BaseVideoLiked bean = JSONUtils.fromJson(message, BaseVideoLiked.class);
            ThrowsUtils.throwIf(CommonHandle.isNull(bean), ErrorCode.PARAMS_ERROR);

            if (KafkaConfig.EVENT_TYPE_UPVOTE.equals(eventType)) {
                processUpvote(bean);
            } else if (KafkaConfig.EVENT_TYPE_UNUPVOTE.equals(eventType)) {
                processUnupvote(bean);
            }

            updateLikesCount(bean);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("处理点赞消息失败，发送到死信队列: {}", message, e);
            kafkaTemplate.send(KafkaConfig.TOPIC_VIDEO_LIKES_DLT, message);
            ack.acknowledge();
        }
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_VIDEO_LIKES_DLT,
            groupId = "video-likes-dlt-group"
    )
    public void handleDeadLetter(String message) {
        log.error("死信队列消息: {}", message);
    }

    private void processUpvote(BaseVideoLiked bean) {
        String lockKey = VIDEO_LIKES_LOCK + bean.getUserId() + ":" + bean.getVideoId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("获取点赞锁失败，跳过处理: userId={}, videoId={}", bean.getUserId(), bean.getVideoId());
                return;
            }
            LambdaQueryWrapper<VideoLikes> existsWrapper = new LambdaQueryWrapper<>();
            existsWrapper.eq(VideoLikes::getUserId, bean.getUserId())
                    .eq(VideoLikes::getVideoId, bean.getVideoId());
            VideoLikes exists = videoLikesService.getOne(existsWrapper);
            if (exists != null) {
                log.info("点赞记录已存在，跳过重复处理: userId={}, videoId={}", bean.getUserId(), bean.getVideoId());
                return;
            }
            VideoLikes likes = new VideoLikes();
            likes.setIsActive(1);
            likes.setUserId(bean.getUserId());
            likes.setVideoId(bean.getVideoId());
            boolean result = videoLikesService.save(likes);
            ThrowsUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR);
        } catch (InterruptedException e) {
            log.error("获取点赞锁被中断: userId={}, videoId={}", bean.getUserId(), bean.getVideoId(), e);
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void processUnupvote(BaseVideoLiked bean) {
        String lockKey = VIDEO_LIKES_LOCK + bean.getUserId() + ":" + bean.getVideoId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("获取取消点赞锁失败，跳过处理: userId={}, videoId={}", bean.getUserId(), bean.getVideoId());
                return;
            }
            LambdaQueryWrapper<VideoLikes> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(VideoLikes::getVideoId, bean.getVideoId())
                    .eq(VideoLikes::getUserId, bean.getUserId());
            VideoLikes exists = videoLikesService.getOne(wrapper);
            if (exists == null) {
                log.info("点赞记录不存在，跳过取消点赞: userId={}, videoId={}", bean.getUserId(), bean.getVideoId());
                return;
            }
            boolean result = videoLikesService.remove(wrapper);
            ThrowsUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR);
        } catch (InterruptedException e) {
            log.error("获取取消点赞锁被中断: userId={}, videoId={}", bean.getUserId(), bean.getVideoId(), e);
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void updateLikesCount(BaseVideoLiked bean) {
        LambdaQueryWrapper<VideoLikes> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoLikes::getVideoId, bean.getVideoId());
        long count = videoLikesService.count(wrapper);
        videoServiceFeignClient.updateVideoLikes(bean.getVideoId(), count);
    }
}
