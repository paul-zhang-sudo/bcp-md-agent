package com.bsi.md.agent.datasource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.sap.AgRFCManager;
import com.bsi.utils.JSONUtils;
import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.DestinationDataProvider;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * sapRfc类型数据源模板
 * @author fish
 */
@Data
public class AgSapRFCTemplate implements AgDataSourceTemplate{
    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    //key
    private String destName;

    //应用服务器ip
    private String serverIp;

    //服务端编号
    private String serverNo;

    //客户端编号
    private String clientNo;

    //用户名
    private String userName;

    //密码
    private String password;

    //其他参数
    private Map<String,String> otherParams;

    //jco
    private JCoDestination jCoDestination;

    public AgSapRFCTemplate(String destName,String serverIp,String serverNo,String clientNo,String userName,String password,Map<String,String> otherParams){
        this.destName = destName;
        this.serverIp = serverIp;
        this.serverNo = serverNo;
        this.clientNo = clientNo;
        this.userName = userName;
        this.password = password;
        this.otherParams = otherParams;

        Properties connectProperties = new Properties();
        connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, serverIp);
        connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR,  serverNo);
        connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, clientNo);
        connectProperties.setProperty(DestinationDataProvider.JCO_USER, userName);
        connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, password);
        connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "en");
        // 最大连接数
        connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, "3");
        // 最大连接线程
        connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, "10");
        if( MapUtils.isNotEmpty(otherParams) ){
            otherParams.forEach((k,v)->{
                connectProperties.setProperty(k,v);
            });
        }
        AgRFCManager.registerProperties(destName,connectProperties);
        AgRFCManager.ping(destName);
        jCoDestination = AgRFCManager.getDestination(destName);
    }
    /**
     * 执行jco函数
     * @param functionName
     * @param param
     * @return
     */
    public Object executeFunction(String functionName, Map<String,Object> param){
        Object result = null;
        try{
            JCoFunction function = jCoDestination.getRepository().getFunction(functionName);
            //设置参数
            if(MapUtils.isNotEmpty(param)){
                JCoParameterList paramList = function.getImportParameterList();
                param.forEach((k,v)->{
                    paramList.setValue(k, v);
                });
            }
            function.execute(jCoDestination);
            result = function.getExportParameterList().toString();
            // 获取RFC返回的字段值
//            JCoParameterList exportParam = function.getExportParameterList();
//            JCoParameterFieldIterator it = exportParam.getParameterFieldIterator();
            // 遍历RFC返回的表对象 TODO 字段和结果是否正确
//            JCoTable tb = function.getTableParameterList().getTable("RESULT_NAME");
//            for (int i = 0; i < tb.getNumRows(); i++) {
//                tb.setRow(i);
//                JSONObject obj = new JSONObject();
//                tb.forEach(f->{
//                    obj.put(f.getName(),f.getString());
//                });
//                list.add(obj);
//            }
        }catch (Exception e){
            info_log.error("调用jco函数报错,错误信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
        return result;
    }

    /**
     * 执行jco函数
     * @param functionName
     * @param param
     * @return
     */
    public Object executeFunctionNew(String functionName, Map<String,Object> param){
        Object result = null;
        try{
            JCoFunction function = jCoDestination.getRepository().getFunction(functionName);
            //设置参数
            if(MapUtils.isNotEmpty(param)){
                JCoParameterList paramList = function.getImportParameterList();
                param.forEach((k,v)->{
                    info_log.info("k:{},v:{}",k,v);
                    paramList.setValue(k, v);
                });
            }
            function.execute(jCoDestination);
            result = function.getExportParameterList().toString();
            // 获取RFC返回的字段值
            JCoParameterList exportParam = function.getExportParameterList();
            JCoParameterFieldIterator it = exportParam.getParameterFieldIterator();
            // 遍历RFC返回的表对象 TODO 字段和结果是否正确
            JSONObject resultObj = new JSONObject(true);
            info_log.info("tables:{}",function.getTableParameterList().toString());
            JCoParameterList tables = function.getTableParameterList();
            Iterator<JCoField> iterator = tables.iterator();
            while (iterator.hasNext()){
                JCoField j = iterator.next();
                info_log.info("JCoField:{},name:{}", JSONUtils.toJson(j),j.getName());
                JCoTable tb = j.getTable();
                JCoRecordMetaData tableMeta = tb.getRecordMetaData();
                for(int i = 0; i < tableMeta.getFieldCount(); i++){
                    info_log.info(String.format("%s\t", tableMeta.getName(i)));
                }

                for (int i = 0; i < tb.getNumRows(); i++) {
                    tb.setRow(i);
                    JSONObject obj = new JSONObject();
                    tb.forEach(f->{
                        obj.put(f.getName(),f.getString());
                    });
                    info_log.info("obj:{}", obj.toJSONString());
                }
            }
        }catch (Exception e){
            info_log.error("调用jco函数报错,错误信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
        return result;
    }
}