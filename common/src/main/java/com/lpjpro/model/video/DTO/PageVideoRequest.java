package com.lpjpro.model.video.DTO;

import com.lpjpro.constant.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 根据分类分页查询视频列表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PageVideoRequest extends PageRequest implements Serializable {
    /**
     * 类名
     */
    private String categoryName;
}
