package com.lpjpro.service;

import com.lpjpro.pojo.UserBehaviorRequest;

import java.util.List;

/**
* @author HL
* @description 针对表【user_behavior(用户行为日志表)】的数据库操作Service
* @createDate 2025-04-23 22:22:00
*/
public interface UserBehaviorService {


    /**
     * 批量插入用户行为数据
     * @param userBehaviorRequests
     * @return
     */
    void batchAdd(List<UserBehaviorRequest> userBehaviorRequests);
}
