package com.lpjpro.pojo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
public class UserLongTermData implements Serializable {
    private String userId;

    private String featureType;

    private String featureValue;

    private Integer watchCount;

    private Integer likeCount;

    private Integer shareCount;

    private Integer commentCount;

    private Integer avgWatchTime;

    private Date date;
}
