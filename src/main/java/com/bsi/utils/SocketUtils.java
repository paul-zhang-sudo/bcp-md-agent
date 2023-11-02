package com.bsi.utils;

import com.bsi.framework.core.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * socket工具类
 */
@Slf4j
public class SocketUtils {
    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");
    private static HashMap<String,SocketClient> clientMap = new HashMap<>();

    public static SocketClient getClient(String key,String ip,int port){
        info_log.info("key:{},ip:{},port:{}",key,ip,port);
        log.info("key:{},ip:{},port:{}",key,ip,port);
        SocketClient client = clientMap.get(key);
        info_log.info("client:{}",client);
        log.info("client:{}",client);
        if(client==null){
            info_log.info("client对象不存在,创建新的client对象");
            client = new SocketClient();
            try {
                client.connect(ip,port);
            }catch (Exception e){
                info_log.error("连接socket服务报错:{}", ExceptionUtils.getFullStackTrace(e));
            }
            clientMap.put(key,client);
        }
        return client;
    }
}
