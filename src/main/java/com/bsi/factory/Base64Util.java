package com.bsi.factory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;

/**
 * Base64工具类
 */
public class Base64Util {
    
    /**
     * Base64编码
     */
	public static String encryptBASE64(byte[] key) {
        return (new BASE64Encoder()).encodeBuffer(key);
    }

    /**
     * Base64解码
     * @throws IOException 
     */
	public static byte[] decryptBASE64(String key) throws IOException {
        return (new BASE64Decoder()).decodeBuffer(key);
    }

}
