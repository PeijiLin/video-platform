package com.lpjpro.model.comment.DTO;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class CommentAddRequest implements Serializable {
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
     * 评论时间
     */
    private Date createdTime;
}
