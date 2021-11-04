package com.bsi.utils;

import com.bsi.md.agent.datasource.AgDatasourceContainer;
import com.bsi.md.agent.datasource.AgJdbcTemplate;

/**
 * sql语句执行工具类,支持对数据库的增删改查
 * @author fish
 */
public class DBUtils {
    /**
     * 通过sql查询数据，返回集合
     * @param sql sql语句
     * @param args 参数数组
     * @param dataSourceId 数据源id
     * @return Object
     */
    public static Object queryForList(String sql,Object[] args,String dataSourceId){
        AgJdbcTemplate template = AgDatasourceContainer.getJdbcDataSource(dataSourceId);
        return template.queryForList(sql,args);
    }

    /**
     * 通过sql查询单条数据，返回单条数据
     * @param sql sql语句
     * @param args 参数数组
     * @param dataSourceId 数据源id
     * @return Object
     */
    public static Object queryForObject(String sql,Object[] args,String dataSourceId) {
        AgJdbcTemplate template = AgDatasourceContainer.getJdbcDataSource(dataSourceId);
        return template.queryForMap(sql, args);
    }

    /**
     * 执行新增、更新、删除数据
     * @param sql sql语句
     * @param args 参数数组
     * @param dataSourceId 数据源id
     * @return int 更新数量
     */
    public static int execute(String sql,Object[] args,String dataSourceId){
        AgJdbcTemplate template = AgDatasourceContainer.getJdbcDataSource(dataSourceId);
        return template.update(sql,args);
    }

    /**
     * 执行新增、更新、删除数据
     * @param sql sql语句
     * @param args 参数数组
     * @param dataSourceId 数据源id
     * @return int 更新数量
     */
//    public static int executeBatch(String sql,Object[] args,Integer dataSourceId){
//        AgJdbcTemplate template = AgDatasourceContainer.getJdbcDataSource(dataSourceId);
//        return template.getJdbcTemplate().ba
//    }
}
