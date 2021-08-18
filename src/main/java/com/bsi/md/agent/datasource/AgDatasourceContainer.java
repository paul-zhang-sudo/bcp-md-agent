package com.bsi.md.agent.datasource;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据源存储容器
 * @author fish
 */
public class AgDatasourceContainer {
    private static Map<Integer,AgJdbcTemplate> jdbcMap = new HashMap<>();
    private static Map<Integer, AgApiTemplate> apiMap = new HashMap<>();

    /**
     * 添加一个jdbc数据源
     * @param key
     * @param template
     */
    public static void addJdbcDataSource(Integer key,AgJdbcTemplate template){
        jdbcMap.put(key,template);
    }

    /**
     * 清空jdbc数据源map
     */
    public static void clearJdbcDataSource(){
        jdbcMap.clear();
    }

    /**
     * 清空api数据源map
     */
    public static void clearApiataSource(){
        apiMap.clear();
    }

    /**
     * 获取jdbc数据源
     * @param key
     * @return
     */
    public static AgJdbcTemplate getJdbcDataSource(Integer key){
        return jdbcMap.get(key);
    }

    /**
     * 添加一个api数据源
     * @param key
     * @param template
     */
    public static void addApiDataSource(Integer key,AgApiTemplate template){
        apiMap.put(key,template);
    }

    /**
     * 获取api数据源
     * @param key
     * @return
     */
    public static AgApiTemplate getApiDataSource(Integer key){
        return apiMap.get(key);
    }
}
