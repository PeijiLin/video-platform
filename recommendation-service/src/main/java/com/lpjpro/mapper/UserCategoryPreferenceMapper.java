package com.lpjpro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lpjpro.model.preference.entity.UserCategoryPreference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserCategoryPreferenceMapper extends BaseMapper<UserCategoryPreference> {

    @Select("SELECT * FROM user_category_preference WHERE user_id = #{userId} ORDER BY score DESC")
    List<UserCategoryPreference> selectByUserId(Long userId);
}
