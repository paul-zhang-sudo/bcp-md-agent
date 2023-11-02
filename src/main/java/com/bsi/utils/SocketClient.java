package com.bsi.utils;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;  
import java.io.InputStreamReader;  
import java.io.OutputStreamWriter;  
import java.net.Socket;
import java.util.HashMap;

public class SocketClient {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
  
    public void connect(String host, int port) throws Exception {
        socket = new Socket(host, port);  
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));  
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
    }
  
    public void sendMessage(String message) throws Exception {
        writer.write(message);
        writer.newLine();
        writer.flush();
    }
  
    public String receiveMessage() throws Exception {
        StringBuilder response = new StringBuilder();
        String line = reader.readLine();
        response.append(line);
        return response.toString();  
    }  
  
    public void disconnect() {
        IOUtils.closeQuietly(writer);
        IOUtils.closeQuietly(reader);
        IOUtils.closeQuietly(socket);
    }
}