package com.bsi.md.agent.sap;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.sap.conn.jco.*;
import com.sap.conn.jco.ext.Environment;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * @Description: rfc调用管理类
 * @author fish
 */
@Slf4j
public class AgRFCManager
{
    private static String ABAP_AS_POOLED = "ABAP_AS_POOL";


    private static AgDestinationDataProvider provider = null;
    private static JCoDestination destination = null;

    /**
     * 初始化provider
     */
    static {
        provider = new AgDestinationDataProvider();
        try {
            Environment.registerDestinationDataProvider(provider);
        } catch (IllegalStateException e) {
            log.error("sap provider注册报错,报错信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
    }

    /**
     * 添加或者修改sap连接信息
     * @param key
     * @param p
     */
    public static void registerProperties(String key,Properties p) {
        provider.changeProperties(key,p);
    }


    /**
     * 获取JCoDestination
     * @param key
     * @return
     * @throws JCoException
     */
    public static JCoDestination getDestination(String key){
        if (destination == null) {
            try {
                destination = JCoDestinationManager.getDestination(key);
            }catch (Exception e){
                log.error("获取destination报错，报错信息:{}",ExceptionUtils.getFullStackTrace(e));
            }
        }
        return destination;
    }

    /**
     * 获取jco函数
     * @param functionName
     * @param destination
     * @return
     */
    public static JCoFunction getFunction(String functionName,JCoDestination destination) {
        JCoFunction function = null;
        try {
            function = destination.getRepository()
                    .getFunctionTemplate(functionName).getFunction();
        } catch (Exception e) {
            log.error("获取jco函数报错,错误信息:{}",ExceptionUtils.getFullStackTrace(e));
        }
        return function;
    }

    /**
     * 执行函数
     * @param function
     * @param destination
     */
    public static void execute(JCoFunction function,JCoDestination destination) {
        JCoParameterList paramList = function.getImportParameterList();
        try {
            function.execute(destination);
        } catch (JCoException e) {
            log.error("Destination error : " + ExceptionUtils.getFullStackTrace(e));
        }
        paramList = function.getExportParameterList();
    }

    /**
     * sap 连接测试
     * @param key
     */
    public static void ping(String key) {
        try
        {
            JCoDestination dest = JCoDestinationManager.getDestination(key);
            dest.ping();
            log.info("Destination " + key + " works");
            log.info("attribute:{}",dest.getAttributes());
        }
        catch(JCoException e)
        {
            log.info("Execution on destination " + key+ " failed, exception:{}",ExceptionUtils.getFullStackTrace(e));
        }
    }

}

