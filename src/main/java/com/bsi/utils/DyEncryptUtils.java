package com.bsi.utils;

import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 大冶污染源立体监控系统加解密工具
 */
public class DyEncryptUtils {
    private final static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    public static String encodeLoginData(String pk,String rstr,String data){
        JSONObject jsonObject = new JSONObject();
        try{
            PublicKey pubKey = getPublicKeyFromPem(pk);
            String encryptedRstr = encryptWithRSA(rstr, pubKey);
            String encryptedJson = encryptWithAES(data, rstr);
            jsonObject.put("p", encryptedRstr);
            jsonObject.put("d", encryptedJson);
            jsonObject.put("srv", "user.account.login");
        }catch (Exception e){
            info_log.info("加密登录数据失败:{}", ExceptionUtils.getFullStackTrace(e));
        }
        return jsonObject.toJSONString();
    }

    // RSA 公钥加密
    public static PublicKey getPublicKeyFromPem(String publicKeyPem) throws Exception {
        String publicKeyPEM = publicKeyPem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    public static String encryptWithRSA(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // AES ECB 模式加密
    public static String encryptWithAES(String jsonString, String aesKeyStr) throws Exception {
        SecretKey aesKey = new SecretKeySpec(aesKeyStr.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedBytes = cipher.doFinal(jsonString.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // AES 解密
    public static String decrypt(String encrypted, String rstr) throws Exception {
        // 将 AES 密钥转换为字节数组
        SecretKey aesKey = new SecretKeySpec(rstr.getBytes(StandardCharsets.UTF_8), "AES");

        // 初始化 AES 解密操作
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);

        // 将加密的 Base64 数据解码为字节数组
        byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);

        // 执行解密操作
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // 将解密后的字节数组转换为字符串并返回
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * md5加密
     * @param value
     * @return String
     */
    public static String md5(String value){
        return MD5.MD5Encode(value);
    }
}
