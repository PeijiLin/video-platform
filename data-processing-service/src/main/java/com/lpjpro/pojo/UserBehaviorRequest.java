package com.lpjpro.pojo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class UserBehaviorRequest implements Serializable {

    /**
     * 用户ID
     */
    @NotNull
    private Long userId;

    /**
     * 视频ID
    */
    @NotNull
    private Long videoId;

    /**
     * 行为类型（play, like, comment, share, pause）
     */
    @NotNull
    private String behaviorType;

    @NotNull
    private Long categoryId;

    /**
     * 行为发生时间
     */
    @NotNull
    private Date timestamp;

    /**
     * 行为持续时间（秒，如播放时长）
     */
    private Integer watchVideoTime;

    /**
     * 地理位置（IP解析）
     */
    private String ipAddress;

    public UserBehaviorRequest() {
    }
}
