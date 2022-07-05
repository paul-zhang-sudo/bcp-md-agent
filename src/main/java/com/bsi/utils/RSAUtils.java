package com.bsi.utils;

import com.alibaba.fastjson.JSON;
import com.bsi.factory.Base64Util;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * RSAUtils RSA工具类
 */
@Slf4j
public class RSAUtils {
    /**
     * 加密
     *
     * @param content 加密内容
     * @param keyStr  私钥
     * @return
     */
    public static String encrypt(String content, String keyStr, boolean isPrivate) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            Key key = isPrivate ? getPrivateKey(keyStr) : getPublicKey(keyStr);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64Util.encryptBASE64((cipher.doFinal(content.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            log.info("加密失败,content:{},ketStr:{}.isPrivate:{},error:{}",content,keyStr,isPrivate,e.getMessage());
            return null;
        }
    }

    /**
     * 解密
     *
     * @param content 待解密内容
     * @param keyStr  私钥
     * @return
     */
    public static String decrypt(String content, String keyStr, boolean isPrivate) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            Key key = isPrivate ? getPrivateKey(keyStr) : getPublicKey(keyStr);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String((cipher.doFinal(Base64Util.decryptBASE64(content))));
        } catch (Exception e) {
            log.info("解密失败,content:{},ketStr:{}.isPrivate:{},error:{}",content,keyStr,isPrivate,e.getMessage());
            return null;
        }
    }

    private static RSAPrivateKey getPrivateKey(String privateKey) throws Exception {
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64Util.decryptBASE64(privateKey)));
    }

    private static RSAPublicKey getPublicKey(String publicKey) throws Exception {
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64Util.decryptBASE64(publicKey)));
    }

    public static String getKeys() throws NoSuchAlgorithmException {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(1024, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   // 得到私钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  // 得到公钥
        String publicKeyString = Base64Util.encryptBASE64(publicKey.getEncoded());
        // 得到私钥字符串
        String privateKeyString = Base64Util.encryptBASE64((privateKey.getEncoded()));
        // 将公钥和私钥保存到Map
        Map<String,String> map = new HashMap<>();
        map.put("publicKey",publicKeyString);
        map.put("privateKey",privateKeyString);
        return JSON.toJSONString(map);
    }
}

