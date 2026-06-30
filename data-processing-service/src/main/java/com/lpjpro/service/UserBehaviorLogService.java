package com.lpjpro.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lpjpro.model.behavior.entity.UserBehaviorLog;

import java.util.List;

/**
 * 用户行为日志服务
 */
public interface UserBehaviorLogService extends IService<UserBehaviorLog> {

    /**
     * 批量写入行为日志到 MySQL
     */
    void batchSave(List<UserBehaviorLog> logs);
}
