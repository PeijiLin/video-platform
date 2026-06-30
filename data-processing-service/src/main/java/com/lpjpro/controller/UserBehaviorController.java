package com.lpjpro.controller;

import cn.hutool.core.bean.BeanUtil;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.model.behavior.entity.UserBehaviorLog;
import com.lpjpro.pojo.UserBehaviorRequest;
import com.lpjpro.service.UserBehaviorLogService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户行为控制器
 */
@RestController
@RequestMapping("/behavior")
public class UserBehaviorController {

    @Resource
    private UserBehaviorLogService userBehaviorLogService;

    /**
     * 批量插入用户行为数据（直接写入 MySQL）
     */
    @PostMapping("/batch")
    public BaseResponse<String> batchAddBehavior(@RequestBody List<UserBehaviorRequest> userBehaviorRequests) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userBehaviorRequests), ErrorCode.PARAMS_ERROR);
        List<UserBehaviorLog> logs = userBehaviorRequests.stream()
                .map(req -> {
                    UserBehaviorLog log = new UserBehaviorLog();
                    BeanUtil.copyProperties(req, log);
                    return log;
                })
                .toList();
        userBehaviorLogService.batchSave(logs);
        return ResultUtils.success(null, "用户行为添加成功");
    }
}
