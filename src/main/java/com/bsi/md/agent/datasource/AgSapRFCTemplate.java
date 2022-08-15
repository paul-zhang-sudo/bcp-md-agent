package com.bsi.md.agent.datasource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.sap.AgRFCManager;
import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.DestinationDataProvider;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
            otherParams.forEach((k,v)-> connectProperties.setProperty(k,v) );
        }
        AgRFCManager.registerProperties(destName,connectProperties);
        AgRFCManager.ping(destName);
        jCoDestination = AgRFCManager.getDestination(destName);
    }

    /**
     * 执行jco查询函数
     * @param functionName
     * @param param
     * @return
     */
    public Object execute(String functionName, Map<String,Object> param){
        JSONObject resultObj = new JSONObject(true);
        try{
            JCoFunction function = jCoDestination.getRepository().getFunction(functionName);
            //设置参数
            if(MapUtils.isNotEmpty(param)){
                Set<Map.Entry<String, Object>> entries = param.entrySet();
                for (Map.Entry<String, Object> entity:entries) {
                    if(entity.getValue() instanceof ScriptObjectMirror){
                        Collection<Object> a = ((ScriptObjectMirror) entity.getValue()).values();
                        JSONArray arr = JSONArray.parseArray( JSON.toJSONString(a) );
                        JCoTable inputTable = function.getTableParameterList().getTable(entity.getKey());
                        for (int j=0;j<arr.size();j++){
                            inputTable.appendRow();
                            JSONObject obj = arr.getJSONObject(j);
                            obj.forEach((k,v)->
                                    inputTable.setValue(k,v)
                            );
                        }
                    }else{
                        JCoParameterList paramList = function.getImportParameterList();
                        param.forEach((k,v)-> paramList.setValue(k, v));
                    }
                }
            }
            function.execute(jCoDestination);
            //返回msg
            JCoParameterList exportParam = function.getExportParameterList();
            if(exportParam!=null){
                Iterator<JCoField> ir = exportParam.iterator();
                while(ir.hasNext()){
                    JCoField o = ir.next();
                    JSONObject et = new JSONObject();
                    Iterator<JCoField> sr = o.getStructure().iterator();
                    while (sr.hasNext()){
                        JCoField s = sr.next();
                        et.put(s.getName(),s.getValue());
                    }
                    resultObj.put(o.getName(),et);
                }
            }
            // 遍历RFC返回的表对象
            JCoParameterList tables = function.getTableParameterList();
            if(tables!=null){
                Iterator<JCoField> iterator = tables.iterator();
                while (iterator.hasNext()){
                    JCoField j = iterator.next();
                    if( param.containsKey(j.getName()) ){
                        continue;
                    }
                    JCoTable tb = j.getTable();
                    JSONArray detail = new JSONArray();
                    for (int i = 0; i < tb.getNumRows(); i++) {
                        tb.setRow(i);
                        JSONObject obj = new JSONObject();
                        tb.forEach(f->{
                            obj.put(f.getName(),f.getString());
                        });
                        detail.add(obj);
                    }
                    resultObj.put(j.getName(),detail);
                }
            }
        }catch (Exception e){
            resultObj.put("sapError",e.getMessage());
            info_log.error("调用jco函数报错,错误信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
        return resultObj;
    }
}