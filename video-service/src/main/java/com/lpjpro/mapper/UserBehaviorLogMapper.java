package com.lpjpro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lpjpro.model.behavior.entity.UserBehaviorLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserBehaviorLogMapper extends BaseMapper<UserBehaviorLog> {

    @Select("SELECT * FROM user_behavior_log WHERE user_id = #{userId} AND behavior_type = 'play' ORDER BY created_time DESC LIMIT #{limit}")
    List<UserBehaviorLog> selectByUserIdAndPlayType(Long userId, int limit);
}
