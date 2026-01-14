package com.bsi.md.agent.utils;

import com.bsi.utils.DateUtils;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class AgSocketServerUtils {
    private static final byte MSG_DELIMITER_00 = 0x00;
    private static final byte MGS_DELIMITER_0a = 0x0a;

    private static final String PROTOCOL_XGGY = "XGGY"; //湘钢规约
    private static final String PROTOCOL_JTGY = "JTGY"; //静态规约

    private static final Map<String,Byte> delimiterMap = ImmutableMap.of(PROTOCOL_XGGY,MSG_DELIMITER_00,PROTOCOL_JTGY,MGS_DELIMITER_0a);

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

    public static String spaceX(int x){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < x; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String getAckMsg(String protocol,String msgNo){
        String clientNo = msgNo.substring(0,2);
        String serverNo = msgNo.substring(2,4);
        String strDate = DateUtils.nowDate("yyyyMMddhhmmss");
        String msg = "";
        switch (protocol.toUpperCase()) {
            case "XGGY":
                //湘钢规约
                msg = handleXGGY(msgNo,strDate,clientNo,serverNo);
                break;
            case "JTGY":
                //静态规约
                msg = handleJTGY(msgNo,strDate,clientNo,serverNo);
                break;
        }
        return msg;
    }

    public static Byte getDelimiter(String protocol){
        return delimiterMap.get(protocol);
    }

    private static String handleXGGY(String msgNo,String strDate,String clientNo,String serverNo){
        String callBackMsg = "0041" + "999998" + strDate.substring(0, 8) +
                strDate.substring(8, 14) + serverNo + clientNo + "0000" + AgSocketServerUtils.spaceX(8) +
                AgSocketServerUtils.hexToString("00");
        return callBackMsg;
    }

    private static String handleJTGY(String msgNo,String strDate,String clientNo,String serverNo){
        String callBackMsg = "0110" + msgNo + strDate.substring(0, 8) +
                strDate.substring(8, 14) + serverNo + clientNo + "A" + AgSocketServerUtils.spaceX(80) +
                AgSocketServerUtils.hexToString("0a");
        return callBackMsg;
    }


}
