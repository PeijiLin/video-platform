package com.lpjpro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lpjpro.model.user.VO.UserVO;
import com.lpjpro.model.user.entity.User;

import java.util.List;

/**
 * @author HL
 * @description 针对表【user(用户信息表)】的数据库操作Mapper
 * @createDate 2025-03-13 16:58:05
 * @Entity generator.domain.User
 */
public interface UserMapper extends BaseMapper<User> {

    List<UserVO> batchSelectIds(List<Long> ids);
}