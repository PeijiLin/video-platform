package com.lpjpro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lpjpro.model.video.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VideoCategoryMapper extends BaseMapper<Video> {

    @Select("SELECT * FROM video WHERE category_id = #{categoryId} ORDER BY reviewed_time DESC LIMIT #{limit}")
    List<Video> selectByCategoryId(Long categoryId, int limit);
}
