package com.bsi.utils;

import com.bsi.framework.core.vo.resp.PageResp;
import com.bsi.md.agent.datasource.AgDatasourceContainer;
import com.bsi.md.agent.datasource.AgJdbcTemplate;
import com.github.pagehelper.PageHelper;
import jdk.nashorn.internal.objects.NativeArray;

import java.util.ArrayList;
import java.util.List;

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

    public static Object queryListPage(String sql,Object[] args,Integer currentPage,Integer pageSize,String dataSourceId) {
        PageHelper.startPage(currentPage,pageSize);
        AgJdbcTemplate template = AgDatasourceContainer.getJdbcDataSource(dataSourceId);
        PageResp res = template.queryListPage(sql,args);
        return JSONUtils.toJson(res);
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
    public static int[] executeBatch(String sql, Object[] args, String dataSourceId){
        AgJdbcTemplate template = AgDatasourceContainer.getJdbcDataSource(dataSourceId);
        List<Object[]> list = new ArrayList<>();
        for(Object obj:args){
            NativeArray arr = (NativeArray) obj;
            list.add(arr.asObjectArray());
        }
        return template.getJdbcTemplate().batchUpdate(sql, list);
    }

}
