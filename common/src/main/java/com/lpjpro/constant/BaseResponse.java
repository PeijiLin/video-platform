package com.lpjpro.constant;

import com.lpjpro.exception.ErrorCode;
import lombok.Getter;
import java.io.Serializable;

@Getter
public class BaseResponse<T> implements Serializable {
    private Integer code;
    private T data;
    private String msg;

    public BaseResponse() {
    }

    public BaseResponse(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public BaseResponse(int code, T data) {
        this(code,data,"");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(),null,errorCode.getMessage());
    }
}
