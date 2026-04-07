package com.lpjpro.service;

/**
 * 数据处理
 */
public interface ProcessDataService {

    /**
     * 用户长期数据
     */
    void userLongTermData();

    /**
     * 用户短期数据
     */
    void userShortTermData() throws Exception;

    /**
     * 视频特征数据
     */
    void videoFeaturesData();

}
