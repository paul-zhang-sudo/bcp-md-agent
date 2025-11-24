package com.bsi.utils;

import com.alibaba.fastjson.JSON;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.IpUtils;
import com.bsi.md.agent.utils.AgSocketServerUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * socket客户端
 */
public class SocketServerN {
    private final AtomicInteger clientCount = new AtomicInteger(0);
    private ServerSocket serverSocket;
    private ExecutorService executor = null;
    public Map<String, LinkedList<String>> msgMap = new HashMap<>();
    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    public void start(int port, int maxClient,String protocol,boolean callBack) throws Exception {
        info_log.info("开始启动socket服务端,内网IP:{}，外网IP:{},端口号:{}", IpUtils.INTRANET_IP, IpUtils.INTERNET_IP, port);
        serverSocket = new ServerSocket(port);
        executor = createThreadPool(maxClient, port);
        try {
            info_log.info("服务端启动，监听端口:{}", port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                int count = clientCount.incrementAndGet();
                clientSocket.setSoTimeout(120000); //设置超时时间
                info_log.info("客户端连接，ip:{},port:{}", clientSocket.getInetAddress(), clientSocket.getPort());
                info_log.info("count:{},maxClient:{}", count, maxClient);
                if (maxClient >= count) {
                    // 尝试提交客户端处理任务到线程池
                    executor.execute(new ClientHandlerN(clientSocket, callBack,protocol));
                    info_log.info("已接入第{}个客户端", count);
                } else {
                    info_log.info("只允许{}个客户端连接", maxClient);
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
    public String receiveMessage() {
        Map<String, ArrayList<String>> map = new HashMap<>();
        msgMap.keySet().forEach(k -> {
            LinkedList<String> msgList = msgMap.get(k);
            ArrayList<String> arr = new ArrayList<>();
            String line = null;
            while ((line = msgList.poll()) != null) {
                arr.add(line);
            }
            if (arr.size() > 0) {
                map.put(k, arr);
            }
        });
        return JSON.toJSONString(map);
    }

    public void disconnect() {
        IOUtils.closeQuietly(serverSocket);
        shutdownThreadPool(executor);
    }

    private ExecutorService createThreadPool(int maxClients, int port) {
        int corePoolSize = 20; // 核心线程数
        int maximumPoolSize = maxClients; // 最大线程数
        long keepAliveTime = 60L; // 空闲线程存活时间（秒）
        TimeUnit unit = TimeUnit.SECONDS; // 时间单位
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

        // 创建线程工厂，用于创建线程
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("SocketServer-%d").build();

        // 创建可回收的线程池
        ExecutorService executor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy() // 当拒绝策略为 CallerRunsPolicy 时，调用者所在的线程会执行该任务
        );
        return executor;
    }

    private static void shutdownThreadPool(ExecutorService executor) {
        if (executor == null) {
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

    class ClientHandlerN implements Runnable {
        private LinkedList<String> msgList = new LinkedList<>();
        private BufferedWriter out = null;
        private InputStream in = null;
        private Socket clientSocket;
        private Boolean callback;
        private String protocol; //协议
        public ClientHandlerN(Socket clientSocket,boolean callback,String protocol) {
            this.clientSocket = clientSocket;
            this.callback = callback;
            this.protocol = protocol;
        }
        public void run(){
            String key = clientSocket.getInetAddress().getHostAddress()+":"+clientSocket.getPort();
            try {
                out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                in = clientSocket.getInputStream();
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream message = new ByteArrayOutputStream();
                while (true) {
                    int bytesRead = in.read(buffer);
                    if ( bytesRead == -1 ) {
                        //如果客户端直接断开连接，这个时候readLine()会返回null导致死循环，cpu占用率100%，所以读取到null要直接断开连接并关闭资源
                        info_log.info("客户端{}已经断开连接",key);
                        break;
                    }
                    for (int i = 0; i < bytesRead; i++) {
                        byte b = buffer[i];
                        if (b == AgSocketServerUtils.getDelimiter(protocol)) {
                            // 处理完整的消息
                            handleMessage(message.toByteArray(),key);
                            message.reset();
                        } else {
                            message.write(b);
                        }
                    }
                }
            } catch (Exception e) {
                info_log.info("客户端处理异常：{}，关闭",ExceptionUtils.getFullStackTrace(e));
            } finally {
                try {
                    info_log.info("开始关闭客户端{}的资源",key);
                    if (out != null) out.close();
                    if (in != null) in.close();
                    if (clientSocket != null && !clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                    out = null;
                    in =null;
                    clientSocket = null;
                    clientCount.decrementAndGet(); //减少客户端
                    info_log.info("资源关闭完毕");
                } catch (Exception e) {
                    info_log.info("资源关闭异常：{}",ExceptionUtils.getFullStackTrace(e));
                }
            }
        }

        private void handleMessage(byte[] message,String key) throws IOException {
            String line = new String(message, StandardCharsets.UTF_8);
            if( line.length()<10 ){
                info_log.info("客户端:{},消息长度小于10,不是正常报文,msg:{}",key, line);
                return;
            }
            String msgNo = line.substring(4,10); //获取报文号
            if( "999999".equals(msgNo) ){
                info_log.info("客户端:{},心跳报文,msg:{}",key, line);
                return;
            }

            msgList.offer(line);
            msgMap.put(key,msgList);
            if(callback) {
                out.write(AgSocketServerUtils.getAckMsg(protocol,msgNo));
                out.flush();
            }
        }
    }
}