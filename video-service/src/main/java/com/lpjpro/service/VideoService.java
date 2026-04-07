package com.lpjpro.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lpjpro.model.ElasticIndex;
import com.lpjpro.model.video.DTO.PageVideoRequest;
import com.lpjpro.model.video.DTO.VideoSearchRequest;
import com.lpjpro.model.video.DTO.VideoUpdateRequest;
import com.lpjpro.model.video.DTO.VideoUploadRequest;
import com.lpjpro.model.video.VO.GetVideoVO;
import com.lpjpro.model.video.VO.VideoPageVO;
import com.lpjpro.model.video.entity.Video;

import java.io.IOException;
import java.util.List;

/**
* @author HL
* @description 针对表【video(视频信息表)】的数据库操作Service
* @createDate 2025-03-13 16:58:49
*/
public interface VideoService extends IService<Video> {

    /**
     * 根据类型分页查询视频列表
     * @param pageVideoRequest 类型名
     * @return 视频列表
     */
    IPage<VideoPageVO> pageVideo(PageVideoRequest pageVideoRequest);


    /**
     * 用户上传视频
     * @param videoUploadRequest 视频信息
     * @return 视频id
     */
    Long uploadVideo(VideoUploadRequest videoUploadRequest) throws IOException;

    /**
     * 获取观看历史
     * @return 视频列表
     */
    List<GetVideoVO> getWatchHistory();

    /**
     * 根据关键词所有视频
     *
     * @param videoSearchRequest@return
     */
    List<ElasticIndex> searchVideo(VideoSearchRequest videoSearchRequest) throws IOException;

    void updateVideo(VideoUpdateRequest videoUpdateRequest);
}
