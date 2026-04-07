package com.lpjpro.controller;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.constant.BaseUserInfo;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.model.*;
import com.lpjpro.service.VideoUploadService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.ResultUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 视频上传相关接口
 * @author lpjpro
 */
@RestController
@RequestMapping("/video/upload")
@RequiredArgsConstructor
@Slf4j
public class VideoUploadController {

    private final VideoUploadService uploadService;

    /**
     * 从 SecurityContext 获取当前用户ID
     */
    private Long getCurrentUserId() {
        return BaseUserInfo.getCurrentUserId();
    }

    /**
     * 1. 初始化分片上传
     * POST /upload/init
     */
    @PostMapping("/init")
    public BaseResponse<InitMultipartResponse> initMultipartUpload(
            @RequestBody @Valid InitMultipartRequest request) throws InterruptedException {
        ThrowsUtils.throwIf(CommonHandle.isNull(request), ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(uploadService.initMultipartUpload(request));
    }

    /**
     * 2. 获取分片预签名URL
     * GET /upload/part-signature
     */
    @GetMapping("/signature")
    public BaseResponse<PartSignatureResponse> getPartSignature(PartSignatureRequest request) {
        // 参数校验
        ThrowsUtils.throwIf(CommonHandle.isNull(request),
                           ErrorCode.PARAMS_ERROR);

        PartSignatureResponse response = uploadService.getPartSignature(request);
        return ResultUtils.success(response);
    }

    /**
     * 3. 完成分片上传
     * POST /upload/complete
     */
    @PostMapping("/complete")
    public BaseResponse<CompleteMultipartResponse> completeMultipartUpload(
            @RequestBody @Valid CompleteMultipartRequest request) {

        Long userId = getCurrentUserId();
        ThrowsUtils.throwIf(CommonHandle.isNull(userId), ErrorCode.NO_AUTH_ERROR);

        CompleteMultipartResponse response = uploadService.completeMultipartUpload(request, userId);
        return ResultUtils.success(response);
    }

    /**
     * 4. 提交视频信息
     * POST /upload/video-info
     */
    @PostMapping("/video-info")
    public BaseResponse<SubmitVideoInfoResponse> submitVideoInfo(
            @ModelAttribute @Valid SubmitVideoInfoRequest request) {

        Long userId = getCurrentUserId();
        ThrowsUtils.throwIf(CommonHandle.isNull(userId), ErrorCode.NO_AUTH_ERROR);

        SubmitVideoInfoResponse response = uploadService.submitVideoInfo(request, userId);
        return ResultUtils.success(response);
    }

    /**
     * 5. 取消上传
     * DELETE /upload/{uploadId}
     */
    @DeleteMapping("/{uploadId}")
    public BaseResponse<Void> cancelUpload(@PathVariable String uploadId) {
        ThrowsUtils.throwIf(CommonHandle.isNull(uploadId), ErrorCode.PARAMS_ERROR);

        Long userId = getCurrentUserId();
        ThrowsUtils.throwIf(CommonHandle.isNull(userId), ErrorCode.NO_AUTH_ERROR);

        // TODO: 实现取消逻辑 - 调用服务层方法
        log.info("用户 {} 请求取消上传，uploadId: {}", userId, uploadId);

        return ResultUtils.success(null, "取消上传请求已提交");
    }
}
