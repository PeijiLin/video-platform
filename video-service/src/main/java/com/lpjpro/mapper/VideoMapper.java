package com.lpjpro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lpjpro.model.video.VO.VideoPageVO;
import com.lpjpro.model.video.entity.Video;
import org.apache.ibatis.annotations.Mapper;

/**
* @author HL
* @description 针对表【video(视频信息表)】的数据库操作Mapper
* @createDate 2025-03-13 16:58:49
* @Entity generator.domain.Video
*/
public interface VideoMapper extends BaseMapper<Video> {

    /**
     * 根据分类id查询相关类型视频信息
     * @param page
     * @param id
     * @param key
     * @return
     */
    IPage<VideoPageVO> selectVideoVO(IPage<VideoPageVO> page, Long id, int key);
}




