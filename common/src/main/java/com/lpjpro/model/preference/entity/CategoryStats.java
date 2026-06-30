package com.lpjpro.model.preference.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 分类统计表（支持冷启动兜底推荐）
 * @TableName category_stats
 */
@Data
public class CategoryStats implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 该分类下已审核通过的视频总数
     */
    private Integer videoCount;

    /**
     * 更新时间
     */
    private Date updatedTime;
}
