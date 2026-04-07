package com.lpjpro.service.impl;

import com.lpjpro.pojo.UserBehaviorRequest;
import com.lpjpro.producer.EventProducer;
import com.lpjpro.service.UserBehaviorService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author HL
* @description 针对表【user_behavior(用户行为日志表)】的数据库操作Service实现
* @createDate 2025-04-23 22:22:00
*/
@Service
public class UserBehaviorServiceImpl implements UserBehaviorService {

    @Resource
    private EventProducer eventProducer;

    /**
     * 批量插入用户行为数据到kafka
     * @param userBehaviorRequests
     * @return
     */
    @Override
    public void batchAdd(List<UserBehaviorRequest> userBehaviorRequests) {
        eventProducer.sentEvent(userBehaviorRequests, "user-behavior");
    }
}




