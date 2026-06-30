package com.lpjpro.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpjpro.mapper.UserBehaviorLogMapper;
import com.lpjpro.model.behavior.entity.UserBehaviorLog;
import com.lpjpro.service.UserBehaviorLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户行为日志服务实现
 */
@Service
public class UserBehaviorLogServiceImpl extends ServiceImpl<UserBehaviorLogMapper, UserBehaviorLog>
    implements UserBehaviorLogService {

    @Override
    public void batchSave(List<UserBehaviorLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        baseMapper.batchInsert(logs);
    }
}
