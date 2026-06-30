package com.lpjpro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lpjpro.model.preference.entity.UserCategoryPreference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户分类偏好 Mapper
 */
@Mapper
public interface UserCategoryPreferenceMapper extends BaseMapper<UserCategoryPreference> {

    /**
     * 插入或更新用户分类偏好
     */
    int upsert(@Param("userId") Long userId,
               @Param("categoryId") Long categoryId,
               @Param("watchCount") Integer watchCount,
               @Param("likeCount") Integer likeCount,
               @Param("commentCount") Integer commentCount,
               @Param("favoriteCount") Integer favoriteCount,
               @Param("totalWatchTime") Integer totalWatchTime,
               @Param("score") Double score);

    /**
     * 根据用户ID和分类ID查询偏好
     */
    UserCategoryPreference selectByUserAndCategory(@Param("userId") Long userId,
                                                   @Param("categoryId") Long categoryId);
}
