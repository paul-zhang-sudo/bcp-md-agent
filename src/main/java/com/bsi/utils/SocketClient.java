package com.bsi.utils;

import com.bsi.md.agent.utils.AgSocketServerUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * socket客户端
 */
public class SocketClient {
    private Socket socket;
    private BufferedWriter writer;
    private InputStream reader;

    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    public void connect(String host, int port) throws Exception {
        info_log.info("开始连接socket服务器,地址：{}，端口号:{}",host,port);
        socket = new Socket(host, port);
        info_log.info("socket连接状态:{}",socket.isConnected());
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = socket.getInputStream();
    }
  
    public void sendMessage(String message) throws Exception {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }

    public void sendMessageNoLine(String message) throws Exception {
        writer.write(message);
        writer.flush();
    }
  
    public String receiveMsgByProtocol(String protocol) throws Exception {
        StringBuilder message = new StringBuilder();
        int byteRead;
        while ((byteRead = reader.read()) != -1) {
            if (byteRead == AgSocketServerUtils.getDelimiter(protocol)) {
                break; // 检测到分隔符，停止读取
            }
            message.append((char) byteRead);
        }
        return message.toString();
    }

    public void disconnect() {
        IOUtils.closeQuietly(writer);
        IOUtils.closeQuietly(reader);
        IOUtils.closeQuietly(socket);
    }
}