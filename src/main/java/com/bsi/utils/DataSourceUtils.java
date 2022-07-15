package com.bsi.utils;

import com.alibaba.fastjson.JSONObject;
import com.bsi.md.agent.datasource.AgDatasourceContainer;

/**
 * 数据源工具类
 */
public class DataSourceUtils {
    /**
     * 获取数据源的属性配置
     * @param dsId
     * @return JSONObject
     */
    public static JSONObject getProp(String dsId){
        return AgDatasourceContainer.getDSProperties(dsId);
    }
}
