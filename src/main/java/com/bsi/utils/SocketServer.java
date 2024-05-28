package com.bsi.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.IpUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/**
 * socket客户端
 */
public class SocketServer {
    private ServerSocket serverSocket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private ExecutorService executor = null;
    Map<String, LinkedList<String>> msgMap = new HashMap<>();
    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    public void start(int port,int maxClient,boolean callBack) throws Exception {
        info_log.info("开始启动socket服务端,内网IP:{}，外网IP:{},端口号:{}", IpUtils.INTRANET_IP,IpUtils.INTERNET_IP,port);
        serverSocket = new ServerSocket(port);
        executor = createThreadPool(maxClient,port);
        try {
            info_log.info("服务端启动，监听端口:{}",port );
            int count=1;
            while (true) {
                Socket clientSocket = serverSocket.accept();
                info_log.info("客户端连接，ip:{},port:{}",clientSocket.getInetAddress(),clientSocket.getPort() );
                info_log.info("count:{},maxClient:{}",count,maxClient);
                if(maxClient>=count){
                    // 尝试提交客户端处理任务到线程池
                    executor.execute(() -> handleClient(clientSocket,callBack));
                    info_log.info("已接入第{}个客户端",count);
                }else{
                    info_log.info("只允许{}个客户端连接",maxClient);
                    // 拒绝新连接，因为线程池已满
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println("客户端已满，没有空闲连接，请稍后再试。");
                    out.flush();
                    try {
                        clientSocket.close();
                    } catch (IOException ioException) {
                        info_log.error("关闭客户端异常:{}", ExceptionUtils.getFullStackTrace(ioException));
                    }
                }
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdownThreadPool(executor); // 关闭线程池
        }
    }

    /**
     * 获取socket消息
     */
    public String receiveMessage(){
        Map<String,ArrayList<String>> map = new HashMap<>();
        msgMap.keySet().forEach(k->{
            LinkedList<String> msgList = msgMap.get(k);
            ArrayList<String> arr = new ArrayList<>();
            String line = null;
            while( (line=msgList.poll())!=null ){
                arr.add(line);
            }
            if(arr.size()>0){
                map.put(k,arr);
            }
        });
        return JSON.toJSONString(map);
    }

    public void disconnect() {
        IOUtils.closeQuietly(writer);
        IOUtils.closeQuietly(reader);
        IOUtils.closeQuietly(serverSocket);
        shutdownThreadPool(executor);
    }

    private ExecutorService createThreadPool(int maxClients,int port) {
        return Executors.newFixedThreadPool(maxClients, (Runnable r) -> {
            Thread t = new Thread(r, "SocketServer"+port);
            t.setDaemon(true); // 设置为守护线程，JVM可以在主线程结束后退出
            return t;
        });
    }

    private static void shutdownThreadPool(ExecutorService executor) {
        if(executor==null){
            return;
        }
        ((ThreadPoolExecutor) executor).shutdown(); // 调用shutdown开始关闭过程
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) { // 等待最多60秒
                executor.shutdownNow(); // 强制关闭未完成的任务
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt(); // 重新设置中断标志
        }
    }

    private void handleClient(Socket clientSocket,boolean callback) {
        LinkedList<String> msgList = new LinkedList<>();
        BufferedWriter out = null;
        BufferedReader in = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (true) {
                String line = in.readLine();
                if(line!=null && line.length()>30){
                    msgList.offer(line);
                    msgMap.put(clientSocket.getInetAddress().getHostAddress()+":"+clientSocket.getPort(),msgList);
                    if(callback) {
                        String strDate =DateUtils.nowDate("yyyyMMddhhmmss");
                        StringBuilder callBackMsg = new StringBuilder();
                        callBackMsg.append("0100").append(line.substring(4,10)).append(strDate.substring(0,8));
                        callBackMsg.append(strDate.substring(8,14)).append("EF").append("PB").append("A").append(space80());
                        callBackMsg.append(hexToString("0a"));
                        out.write(callBackMsg.toString());
                        out.flush();
                    }
                }else{
                    if(line!=null) {
                        info_log.info("line:{}", line);
                    }
                }
            }
        } catch (IOException e) {
            info_log.error("客户端处理异常：{}，关闭",ExceptionUtils.getFullStackTrace(e));
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(":"+hexToString("0a")+":");
    }
    private static String stringToHex(String inputStr) {
        StringBuilder hexBuilder = new StringBuilder();
        for (char ch : inputStr.toCharArray()) {
            // 获取字符的ASCII值并转换为16进制字符串，使用String.format保持两位数格式
            String hex = String.format("%02X", (int) ch);
            hexBuilder.append(hex);
        }
        return hexBuilder.toString();
    }

    private static String hexToString(String hexStr) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hexStr.length(); i += 2) {
            // 从hexStr中取出两个字符构成一个16进制数
            String str = hexStr.substring(i, i + 2);
            // 将16进制字符串转换为十进制整数
            int ascii = Integer.parseInt(str, 16);
            // 将整数转换为字符并添加到输出字符串中
            output.append((char) ascii);
        }
        return output.toString();
    }

    private static String space80(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 80; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
}