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
    private static HashMap<String,SocketServerN> serverMap = new HashMap<>();

    /**
     * 生产socket客户端
     * @param key
     * @param ip
     * @param port
     * @return
     */
    public static SocketClient getClient(String key,String ip,int port){
        info_log.info("key:{},ip:{},port:{}",key,ip,port);
        SocketClient client = clientMap.get(key);
        info_log.info("client:{}",client);
        if(client==null){
            info_log.info("client对象不存在,创建新的client对象");
            client = new SocketClient();
            try {
                client.connect(ip,port);
                clientMap.put(key,client);
            }catch (Exception e) {
                info_log.error("连接socket服务报错:{}", ExceptionUtils.getFullStackTrace(e));
            }
        }
        return client;
    }

    /**
     * 生产socket客户端
     * @param key
     * @param port
     * @return
     */
    public static SocketServerN createServer(String key,int port,int maxClient,String protocol,boolean callBack){
        info_log.info("key:{},port:{}",key,port);
        SocketServerN server = serverMap.get(key);
        info_log.info("server:{}",server);
        if(server==null){
            info_log.info("server对象不存在,创建新的server对象");
            server = new SocketServerN();
            try {
                serverMap.put(key,server);
                server.start(port,maxClient,protocol,callBack);
            }catch (Exception e) {
                info_log.error("连接socket服务报错:{}", ExceptionUtils.getFullStackTrace(e));
            }
        }
        return server;
    }

    /**
     * 生产socket客户端
     * @param key
     * @return
     */
    public static SocketServerN getServer(String key){
        info_log.info("key:{}",key);
        SocketServerN server = serverMap.get(key);
        info_log.info("server:{}",server);
        return server;
    }
}
