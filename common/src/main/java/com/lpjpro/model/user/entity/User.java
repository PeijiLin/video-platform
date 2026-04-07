package com.lpjpro.model.user.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户信息表
 * @TableName user
 */
@Data
public class User implements Serializable {
    /**
     * 用户唯一ID
     */
    private Long id;

    /**
     * 账号
     */
    private String account;

    /**
     * 用户名（登录名）
     */
    private String username;

    /**
     * 邮箱（可选）
     */
    private String email;

    /**
     * 手机号（可选）
     */
    private String phone;

    /**
     * 加密后的密码
     */
    private String password;

    /**
     * 头像路径
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 角色：0-普通用户，1-管理员，2-审核员
     */
    private Integer role;

    /**
     * 邮箱是否验证
     */
    private Integer isEmailVerified;

    /**
     * 手机是否验证
     */
    private Integer isPhoneVerified;

    /**
     * 是否激活账号（0-禁用）
     */
    private Integer isActive;

    /**
     * 用户等级：1 VIP 用户，0 普通用户
     */
    private Integer userLevel;

    /**
     * 注册时间
     */
    private Date createdTime;

    /**
     * 最后更新时间
     */
    private Date updatedTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 逻辑删除：0 未删除，1 被删除。
     */
    private Integer isDelete;

    /**
     * 逻辑删除时间（软删除）
     */
    private Date deletedTime;
}