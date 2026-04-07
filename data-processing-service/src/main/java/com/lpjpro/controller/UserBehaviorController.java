package com.lpjpro.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.model.userpicture.entity.UserPicture;
import com.lpjpro.model.video.entity.Video;
import com.lpjpro.pojo.UserBehaviorRequest;
import com.lpjpro.service.UserBehaviorService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@ResponseBody
@RequestMapping("/behavior")
public class UserBehaviorController {

    @Resource
    private UserBehaviorService userBehaviorService;


    /**
     * 批量插入用户行为数据
     * @param userBehaviorRequests
     * @return
     */
    @PostMapping("/batch")
    public BaseResponse<String> batchAddBehavior(@RequestBody List<UserBehaviorRequest> userBehaviorRequests) {
        ThrowsUtils.throwIf(CommonHandle.isNull(userBehaviorRequests), ErrorCode.PARAMS_ERROR);
        userBehaviorService.batchAdd(userBehaviorRequests);
        return ResultUtils.success(null,"用户行为添加成功");
    }
}
