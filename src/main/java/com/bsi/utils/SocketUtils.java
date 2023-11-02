package com.bsi.utils;

import com.bsi.framework.core.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class SocketUtils {
    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");
    private static HashMap<String,SocketClient> clientMap = new HashMap<>();

    public static SocketClient getClient(String key,String ip,int port){
        SocketClient client = clientMap.get(key);
        if(client==null){
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
