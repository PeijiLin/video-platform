package com.lpjpro.exception;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Slf4j
public class ExceptionHandle {
    @org.springframework.web.bind.annotation.ExceptionHandler(BusinessException.class)
    public BaseResponse<?> BusinessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(),e.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(TokenParseException.class)
    public Object handleTokenParseException(TokenParseException e) {
//        request.setAttribute("tokenError", e.getMessage());
        return null; // 返回null表示继续执行原请求
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> BusinessExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage());
    }
}
