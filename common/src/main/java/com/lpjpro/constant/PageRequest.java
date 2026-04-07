package com.lpjpro.constant;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页通用请求
 */
@Data
public class PageRequest implements Serializable {
    /**
     * 每页数量
     */
    private int pageSize = 10;

    /**
     * 页数
     */
    private int PageNum = 1;


}