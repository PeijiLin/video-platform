package com.lpjpro.model.category.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频分类表
 * @TableName category
 */
@Data
public class Category implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     *
     */
    private Date createdTime;

    /**
     * 是否删除（0-未删除，1-删除）
     */
    private Integer isDelete;
}