package com.bsi.utils;

import com.bsi.framework.core.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES加解密工具类
 */
public class AESUtils {

    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");
    public static String encrypt(String plainText,String key,String iv){
        String res ="";
        try{
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            res = Base64.getEncoder().encodeToString(encryptedBytes);

        }catch (Exception e){
            e.printStackTrace();
            info_log.info("AES加密失败，错误信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
        return res;
    }

    public static String decrypt(String cipherText,String key,String iv) {
        String res ="";
        try{
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
            res = new String(cipher.doFinal(decodedBytes), StandardCharsets.UTF_8);
        }catch (Exception e){
            e.printStackTrace();
            info_log.info("AES加密失败，错误信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
        return res;
    }
}