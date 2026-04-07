package com.lpjpro.model.userpicture.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户画像表
 * @TableName user_picture
 */
@Data
public class UserPicture implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户特征数据
     */
    private String userFeatures;

    /**
     * 记录创建时间
     */
    private Date createdTime;

    /**
     * 最后更新时间
     */
    private Date updatedTime;

    /**
     * 是否删除（0-未删除；1-删除）
     */
    private Integer isDelete;
}