package com.bsi.utils;

import com.bsi.framework.core.vo.resp.PageResp;
import com.bsi.md.agent.datasource.AgDatasourceContainer;
import com.bsi.md.agent.datasource.AgJdbcTemplate;
import com.github.pagehelper.PageHelper;
import jdk.nashorn.internal.objects.NativeArray;

import java.util.ArrayList;
import java.util.List;

import java.sql.Connection;
import java.sql.SQLException;

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


    // ===================== 以下为事务相关的方法 =====================
    /* 调用示例：
     DBUtils.beginTransaction(dataSource);
     try {
        // 执行批量更新1
        ……
        // 执行批量更新2
        ……
        // 提交事务
        DBUtils.commitTransaction(dataSource);
     } catch (e) {
        // 出现错误，回滚事务
        DBUtils.rollbackTransaction(dataSource);
        throw e;
     }
     */

    /**
     * 开始事务
     * @param dataSourceId 数据源id
     */
    public static void startTransaction(String dataSourceId) {
        AgJdbcTemplate template = AgDatasourceContainer.getJdbcDataSource(dataSourceId);
        if (template == null || template.getJdbcTemplate().getDataSource() == null) {
            throw new RuntimeException("DataSource or JdbcTemplate is not initialized.");
        }

        try {
            Connection connection = template.getJdbcTemplate().getDataSource().getConnection();
            if (connection == null) {
                throw new RuntimeException("Failed to obtain a connection from the data source.");
            }
            connection.setAutoCommit(false);  // 禁用自动提交
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to start transaction: " + e.getMessage(), e);
        }
    }

    /**
     * 提交事务
     * @param dataSourceId 数据源id
     */
    public static void commitTransaction(String dataSourceId) {
        AgJdbcTemplate template = AgDatasourceContainer.getJdbcDataSource(dataSourceId);
        if (template == null || template.getJdbcTemplate().getDataSource() == null) {
            throw new RuntimeException("DataSource or JdbcTemplate is not initialized.");
        }

        try {
            Connection connection = template.getJdbcTemplate().getDataSource().getConnection();
            if (connection == null) {
                throw new RuntimeException("Failed to obtain a connection from the data source.");
            }
            connection.commit();  // 提交事务
        } catch (SQLException e) {
            e.printStackTrace();
            rollbackTransaction(dataSourceId); // 如果提交失败则回滚
            throw new RuntimeException("Failed to commit transaction: " + e.getMessage(), e);
        } finally {
            try {
                Connection connection = template.getJdbcTemplate().getDataSource().getConnection();
                if (connection != null) {
                    connection.setAutoCommit(true);  // 恢复自动提交模式
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 回滚事务
     * @param dataSourceId 数据源id
     */
    public static void rollbackTransaction(String dataSourceId) {
        AgJdbcTemplate template = AgDatasourceContainer.getJdbcDataSource(dataSourceId);
        if (template == null || template.getJdbcTemplate().getDataSource() == null) {
            throw new RuntimeException("DataSource or JdbcTemplate is not initialized.");
        }

        try {
            Connection connection = template.getJdbcTemplate().getDataSource().getConnection();
            if (connection == null) {
                throw new RuntimeException("Failed to obtain a connection from the data source.");
            }
            connection.rollback();  // 回滚事务
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to rollback transaction: " + e.getMessage(), e);
        } finally {
            try {
                Connection connection = template.getJdbcTemplate().getDataSource().getConnection();
                if (connection != null) {
                    connection.setAutoCommit(true);  // 恢复自动提交模式
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }



}

