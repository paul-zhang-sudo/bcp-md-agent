package com.bsi.md.agent.utils;

public class AgSocketServerUtils {
    public static String stringToHex(String inputStr) {
        StringBuilder hexBuilder = new StringBuilder();
        for (char ch : inputStr.toCharArray()) {
            // 获取字符的ASCII值并转换为16进制字符串，使用String.format保持两位数格式
            String hex = String.format("%02X", (int) ch);
            hexBuilder.append(hex);
        }
        return hexBuilder.toString();
    }

    public static String hexToString(String hexStr) {
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

    public static String space80(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 80; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
