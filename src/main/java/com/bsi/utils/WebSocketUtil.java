package com.bsi.utils;

import com.alibaba.fastjson.JSON;
import com.bsi.framework.core.utils.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class WebSocketUtil {

    public static String getWebSocketMessage(String url,String time){
        String serverUrl = url;//"ws://localhost:8080/ws/incremental"; // Replace with your WebSocket server URL
        final List<String> list = new ArrayList<String>();
        try {
            WebSocketClient client = new WebSocketClient(new URI(serverUrl)) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    System.out.println("Connected to WebSocket server.");
                }

                @Override
                public void onMessage(String message) {
                    list.add(message);
                    System.out.println("Received message ");
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    System.out.println("Connection closed.");
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            };
            client.connect();
            int sleepTime = StringUtils.isEmpty(time) ? 30000 : Integer.parseInt(time);
            Thread.sleep(sleepTime);
            client.close();
            return JSON.toJSONString(list);
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
