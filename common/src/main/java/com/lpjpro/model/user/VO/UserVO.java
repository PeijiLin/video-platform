package com.lpjpro.model.user.VO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class UserVO implements Serializable {
    /**
     * 用户唯一ID
     */
    private Long id;

    /**
     * 用户名
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
     * 头像路径
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 用户等级：1 VIP 用户，0 普通用户
     */
    private Integer userLevel;

    /**
     * 注册时间
     */
    private Date createdTime;


    /**
     * 最后登录时间
     */
    private Date lastLoginTime;
}
