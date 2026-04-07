package com.lpjpro.utils;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.exception.ErrorCode;

public class ResultUtils {
    public static <T> BaseResponse<T> success(T data, String msg) {
        return new BaseResponse<>(200,data,msg);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200,data,"OK");
    }

    public static <T> BaseResponse<T> error(int code, String msg) {
        return new BaseResponse<>(code,null,msg);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }


    public static <T> BaseResponse<T> error(ErrorCode errorCode, String msg) {
        return new BaseResponse<>(errorCode.getCode(),null,msg);
    }

}
