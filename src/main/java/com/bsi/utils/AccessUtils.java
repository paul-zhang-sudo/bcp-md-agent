package com.bsi.utils;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * 微软Access数据库工具类
 */
public class AccessUtils {
    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");
    public static Object query(String filePath,String sql,String password) {
        List<Map> arr = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            Properties props = new Properties();
            if(StringUtils.hasText(password)){
                props.put("password", password);
                props.put("jackcessopener", "com.bsi.md.agent.access.JackcessOpener");
            }
            conn = DriverManager.getConnection("jdbc:ucanaccess://"+filePath,props);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            while(rs.next()){
                Map map = new HashMap();
                for(int i=1; i<= resultSetMetaData.getColumnCount(); i++){
                    String columnName = resultSetMetaData.getColumnName(i);//列名
                    Object columnValue = rs.getObject(i);
                    map.put(columnName,columnValue==null?"":columnValue);
                }
                arr.add(map);
            }
        } catch (Exception e) {
            info_log.info("查询access数据报错:{}",ExceptionUtils.getFullStackTrace(e));
        }finally {
            if(rs!=null){
                try{
                    rs.close();
                }catch (Exception e){

                }
            }
            if(stmt!=null){
                try{
                    stmt.close();
                }catch (Exception e){

                }
            }
            if(conn!=null){
                try{
                    conn.close();
                }catch (Exception e){

                }
            }
        }
        return arr;
    }
}
