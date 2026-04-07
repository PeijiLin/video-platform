package com.lpjpro.model.comment.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 视频评论表
 * @TableName comment
 */
@Data
public class Comment implements Serializable {
    /**
     * 评论唯一ID
     */
    private Long id;

    /**
     * 评论者ID（外键）
     */
    private Long userId;

    /**
     * 所属视频ID（外键）
     */
    private Long videoId;

    /**
     * 父评论ID（用于回复）
     */
    private Long parentCommentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Long likes;

    /**
     * 状态：0-审核中，1-已通过，2-已拒绝
     */
    private Integer status;

    /**
     * 评论时间
     */
    private Date createdTime;

    /**
     * 最后更新时间
     */
    private Date updatedTime;

    /**
     * 逻辑删除时间
     */
    private Date deletedTime;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    private Integer isDeleted;

    /**
     * 审核时间
     */
    private Date reviewedTime;

    /**
     * 审核员ID（外键）
     */
    private Long reviewerId;
}