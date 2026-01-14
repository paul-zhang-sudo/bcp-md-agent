package com.bsi.utils;

import com.bsi.framework.core.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * 测试网络连通性工具类
 */

public class NetworkUtils {

    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    /**
     * 测试localIp能否与远程的主机指定端口建立连接相连
     *
     * @param localIp
     * @param remoteIp
     * @param port
     * @param timeout
     * @return
     */
    public static boolean isReachable(String localIp, String remoteIp,int port, int timeout) {
        boolean isReachable=false;
        try {
            InetAddress localInetAddr = InetAddress.getByName(localIp);
            InetAddress remoteInetAddr = InetAddress.getByName(remoteIp);
            isReachable = NetworkUtils.isReachable(localInetAddr,remoteInetAddr,port,timeout);
        } catch (IOException e) {
            info_log.info("检测网络出现异常,异常信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
        return isReachable;
    }

    /**
     * 获取能与远程主机指定端口建立连接的本机ip地址
     * @param remoteIp
     * @param port
     * @return
     */
    public static String getReachableIP(String remoteIp, int port) {
        String retIP = null;
        try {
            InetAddress remoteAddr = InetAddress.getByName(remoteIp);
            retIP = NetworkUtils.getReachableIP(remoteAddr,port);
        }catch (Exception e){
            info_log.info("getReachableIP检测网络出现异常,异常信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
        return retIP;
    }

    /**
     * 测试localInetAddr能否与远程的主机指定端口建立连接相连
     *
     * @param localInetAddr
     * @param remoteInetAddr
     * @param port
     * @param timeout
     * @return
     */
    private static boolean isReachable(InetAddress localInetAddr, InetAddress remoteInetAddr, int port, int timeout) {
        boolean isReachable = false;
        Socket socket = null;
        try {
            socket = new Socket();
            // 端口号设置为 0 表示在本地挑选一个可用端口进行连接
            SocketAddress localSocketAddr = new InetSocketAddress(localInetAddr, 0);
            socket.bind(localSocketAddr);
            InetSocketAddress endpointSocketAddr = new InetSocketAddress(remoteInetAddr, port);
            socket.connect(endpointSocketAddr, timeout);
            info_log.info("SUCCESS - connection established! Local: "+ localInetAddr.getHostAddress() + " remote: "+ remoteInetAddr.getHostAddress() + " port：" + port);
            isReachable = true;
        } catch (IOException e) {
            info_log.error("FAILRE - Unable to Connect! Local: "+ localInetAddr.getHostAddress() + " remote: "+ remoteInetAddr.getHostAddress() + " port：" + port);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    info_log.error("Error occurred while closing socket:"+ e.getMessage());
                }
            }
        }
        return isReachable;
    }

    /**
     * 获取能与远程主机指定端口建立连接的本机ip地址
     * @param remoteAddr
     * @param port
     * @return
     */
    private static String getReachableIP(InetAddress remoteAddr, int port) {
        String retIP = null;
        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> localAddrs = ni.getInetAddresses();
                while (localAddrs.hasMoreElements()) {
                    InetAddress localAddr = localAddrs.nextElement();
                    if (isReachable(localAddr, remoteAddr, port, 5000)) {
                        retIP = localAddr.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            info_log.error("Error occurred while listing all the local network addresses:"+ e.getMessage());
        }
        if (retIP == null) {
            info_log.info("NULL reachable local IP is found!");
        } else {
            info_log.info("Reachable local IP is found, it is " + retIP);
        }
        return retIP;
    }
}