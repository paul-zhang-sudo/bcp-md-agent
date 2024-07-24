//package com.bsi.utils;
//
//import com.alibaba.fastjson.JSON;
//import com.bsi.framework.core.utils.ExceptionUtils;
//import com.bsi.framework.core.utils.IpUtils;
//import com.bsi.md.agent.utils.AgSocketServerUtils;
//import com.google.common.util.concurrent.ThreadFactoryBuilder;
//import org.apache.commons.io.IOUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.*;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.*;
//import java.util.concurrent.*;
//
///**
// * socket客户端
// */
//public class SocketServer {
//    private ServerSocket serverSocket;
//    private ExecutorService executor = null;
//    Map<String, LinkedList<String>> msgMap = new HashMap<>();
//    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");
//
//    public void start(int port, int maxClient, boolean callBack, String serverNo, String clientNo) throws Exception {
//        info_log.info("开始启动socket服务端,内网IP:{}，外网IP:{},端口号:{}", IpUtils.INTRANET_IP, IpUtils.INTERNET_IP, port);
//        serverSocket = new ServerSocket(port);
//        executor = createThreadPool(maxClient, port);
//        try {
//            info_log.info("服务端启动，监听端口:{}", port);
//            int count = 1;
//            while (true) {
//                Socket clientSocket = serverSocket.accept();
//                clientSocket.setSoTimeout(120000); //设置超时时间
//                info_log.info("客户端连接，ip:{},port:{}", clientSocket.getInetAddress(), clientSocket.getPort());
//                info_log.info("count:{},maxClient:{}", count, maxClient);
//                if (maxClient >= count) {
//                    // 尝试提交客户端处理任务到线程池
//                    executor.execute(new ClientHandler(clientSocket, callBack, serverNo, clientNo,msgMap));
//                    info_log.info("已接入第{}个客户端", count);
//                } else {
//                    info_log.info("只允许{}个客户端连接", maxClient);
//                    // 拒绝新连接，因为线程池已满
//                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
//                    out.println("客户端已满，没有空闲连接，请稍后再试。");
//                    out.flush();
//                    try {
//                        clientSocket.close();
//                    } catch (IOException ioException) {
//                        info_log.error("关闭客户端异常:{}", ExceptionUtils.getFullStackTrace(ioException));
//                    }
//                }
//                count++;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            shutdownThreadPool(executor); // 关闭线程池
//        }
//    }
//
//    /**
//     * 获取socket消息
//     */
//    public String receiveMessage() {
//        Map<String, ArrayList<String>> map = new HashMap<>();
//        msgMap.keySet().forEach(k -> {
//            LinkedList<String> msgList = msgMap.get(k);
//            ArrayList<String> arr = new ArrayList<>();
//            String line = null;
//            while ((line = msgList.poll()) != null) {
//                arr.add(line);
//            }
//            if (arr.size() > 0) {
//                map.put(k, arr);
//            }
//        });
//        return JSON.toJSONString(map);
//    }
//
//    public void disconnect() {
//        IOUtils.closeQuietly(serverSocket);
//        shutdownThreadPool(executor);
//    }
//
//    private ExecutorService createThreadPool(int maxClients, int port) {
//        int corePoolSize = 20; // 核心线程数
//        int maximumPoolSize = maxClients; // 最大线程数
//        long keepAliveTime = 60L; // 空闲线程存活时间（秒）
//        TimeUnit unit = TimeUnit.SECONDS; // 时间单位
//        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
//
//        // 创建线程工厂，用于创建线程
//        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("SocketServer-%d").build();
//
//        // 创建可回收的线程池
//        ExecutorService executor = new ThreadPoolExecutor(
//                corePoolSize,
//                maximumPoolSize,
//                keepAliveTime,
//                unit,
//                workQueue,
//                threadFactory,
//                new ThreadPoolExecutor.CallerRunsPolicy() // 当拒绝策略为 CallerRunsPolicy 时，调用者所在的线程会执行该任务
//        );
//        return executor;
//    }
//
//    private static void shutdownThreadPool(ExecutorService executor) {
//        if (executor == null) {
//            return;
//        }
//        ((ThreadPoolExecutor) executor).shutdown(); // 调用shutdown开始关闭过程
//        try {
//            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) { // 等待最多60秒
//                executor.shutdownNow(); // 强制关闭未完成的任务
//            }
//        } catch (InterruptedException ie) {
//            executor.shutdownNow();
//            Thread.currentThread().interrupt(); // 重新设置中断标志
//        }
//    }
//}
//
//class ClientHandler implements Runnable {
//    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");
//    LinkedList<String> msgList = new LinkedList<>();
//    BufferedWriter out = null;
//    BufferedReader in = null;
//    private Socket clientSocket;
//    private Boolean callback;
//    private String serverNo;
//    private String clientNo;
//    Map<String, LinkedList<String>> msgMap = null;
//    public ClientHandler(Socket clientSocket,boolean callback,String serverNo,String clientNo,Map<String, LinkedList<String>> msgMap) {
//        this.clientSocket = clientSocket;
//        this.callback = callback;
//        this.serverNo = serverNo;
//        this.clientNo = clientNo;
//        this.msgMap = msgMap;
//    }
//    public void run(){
//        String key = clientSocket.getInetAddress().getHostAddress()+":"+clientSocket.getPort();
//        try {
//            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
//            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//            while (true) {
//                String line = in.readLine();
//                if ( line==null ) {
//                    //如果客户端直接断开连接，这个时候readLine()会返回null导致死循环，cpu占用率100%，所以读取到null要直接断开连接并关闭资源
//                    info_log.info("客户端{}已经断开连接",key);
//                    break;
//                }
//                if(line.length()>30){
//                    msgList.offer(line);
//                    msgMap.put(key,msgList);
//                    if(callback) {
//                        String strDate =DateUtils.nowDate("yyyyMMddhhmmss");
//                        StringBuilder callBackMsg = new StringBuilder();
//                        callBackMsg.append("0100").append(line.substring(4,10)).append(strDate.substring(0,8));
//                        callBackMsg.append(strDate.substring(8,14)).append(clientNo).append(serverNo).append("A").append(AgSocketServerUtils.spaceX(80));
//                        callBackMsg.append(AgSocketServerUtils.hexToString("0a"));
//                        out.write(callBackMsg.toString());
//                        out.flush();
//                    }
//                }else{
//                    if(line!=null) {
//                        info_log.info("客户端:{},msg:{}",key, line);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            info_log.info("客户端处理异常：{}，关闭",ExceptionUtils.getFullStackTrace(e));
//        } finally {
//            try {
//                info_log.info("开始关闭客户端{}的资源",key);
//                if (out != null) out.close();
//                if (in != null) in.close();
//                if (clientSocket != null && !clientSocket.isClosed()) {
//                    clientSocket.close();
//                }
//                out = null;
//                in =null;
//                clientSocket = null;
//                info_log.info("资源关闭完毕");
//            } catch (Exception e) {
//                info_log.info("资源关闭异常：{}",ExceptionUtils.getFullStackTrace(e));
//            }
//        }
//    }
//}