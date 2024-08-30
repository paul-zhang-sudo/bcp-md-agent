package com.bsi.utils;

import com.bsi.factory.Base64Util;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.utils.IoTEdgeUtil;
import com.huawei.m2m.edge.daemon.util.TokenHolder;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.Optional;

/**
 * @author fish
 *解密工具类
 */
@Slf4j
public class DecryptUtils {
    /**
     * 华为解密方法
     * @param text
     * @return String
     */
    public static String decryptFromHWCloud(String text){
        String result = "";
        try{
            Optional<String> token= TokenHolder.getToken();
            if(!token.isPresent()) {
                Thread.sleep(60000L);
            }
            result = IoTEdgeUtil.getItClient().decryptDataFromCloud(text);
        }catch (Exception e){
            log.info("调用华为解密方法失败，错误信息:{}", ExceptionUtils.getFullStackTrace(e));
        }
       return result;
    }

    public static SecretKeySpec getSecretKeySpec(byte[] secretKeyBytes , String algorithmName){
        return new SecretKeySpec(secretKeyBytes,algorithmName);
    }

    public static String symDecrypt(SecretKeySpec secretKeySpec,byte[] encryptByte,String algorithmName) throws Exception {
        Cipher cipher = Cipher.getInstance( algorithmName);
        cipher.init(Cipher.DECRYPT_MODE,secretKeySpec);
        byte[] bytes = cipher.doFinal(encryptByte);
        return new String(bytes);
    }

    /**
     * base64解码
     * @param decodeString
     * @return
     */
    public static String base64Decode(String decodeString) throws IOException {
        return new String(Base64Util.decryptBASE64(decodeString));
    }
}
