package com.lpjpro.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class BaseVideoLiked {
    /**
     * 点赞记录的唯一ID
     */
    private Long id;

    /**
     * 用户ID，标识点赞的用户
     */
    private Long userId;

    /**
     * 视频ID，标识被点赞的视频
     */
    private Long videoId;

    /**
     * 点赞时间，记录用户点赞的时间
     */
    private Date likedTime;

    /**
     * 是否点赞
     */
    private Integer isActive;
}
