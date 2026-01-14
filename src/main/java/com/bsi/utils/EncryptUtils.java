package com.bsi.utils;

import com.bsi.factory.KeysFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;
import sun.misc.BASE64Encoder;
import sun.rmi.runtime.Log;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 加密类
 */
@Slf4j
public class EncryptUtils {

	// RSA最大加密明文大小
	private static final int MAX_ENCRYPT_BLOCK = 117;
	private static final int Initialization_Vector_Length = 12;
	private static final String ALGORITHM_AES = "AES";
	private static final String ALGORITHM_RSA = "RSA";
	private static final String ALGORITHM_AES_GCM_NP = "AES/GCM/NoPadding";
	private static final String ALGORITHM_AES_CBC_PKCS5 = "AES/CBC/PKCS5Padding";
	public static String encrypt(String type,String key, String data, Map<String,String> config) throws Exception {
		if (type == null)return null;
		String value = "";
		if (config == null) config = new HashMap<>();
		switch (type){
			case ALGORITHM_AES :
				value = symEncrypt(key,data);
				break;
			case ALGORITHM_RSA :
				value = pubEncrypt(key,data);
				break;
			case ALGORITHM_AES_GCM_NP :
				value = encrypt_AES_GCM_NP(key,data,config.get("iv"));
				break;
			case ALGORITHM_AES_CBC_PKCS5 :
				value = encrypt_AES_CBC_PKCS5(key,data,config.get("iv"));
				break;
			default:
				log.warn("not found encrypt type, type:{}",type);
		}
		return value;
	}

	public static String encrypt_AES_GCM_NP(String secretKey,String data,String IVStr) throws Exception {
		// 使用AESKey进行加密
		byte[] iv;
		if(IVStr == null){
			SecureRandom secureRandom = new SecureRandom();
			iv = new byte[Initialization_Vector_Length]; // GCM 推荐的 IV 长度是 12 字节
			secureRandom.nextBytes(iv);
		}else {
			iv = IVStr.getBytes();
		}
		Cipher cipher = Cipher.getInstance(ALGORITHM_AES_GCM_NP);
        SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM_AES);// secretKey 是同步参数配置的加密秘钥
		GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128,iv);
		cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);
		byte[] bytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
		// 字符串转码后即为加密后的内容
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static String encrypt_AES_CBC_PKCS5(String secretKey,String data,String IVStr) throws Exception {
		//将AES密销转换为SecretKeySpec对象
		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(),ALGORITHM_AES);
		//将AES初始化向量转换为IvParameterSpec对象
		IvParameterSpec ivParameterSpec = new IvParameterSpec(IVStr.getBytes());
		//根据加密算法获取加密器
		Cipher cipher = Cipher.getInstance(ALGORITHM_AES_CBC_PKCS5);
		//初始化加密器，设置加密模式、密钥和初始化向量
		cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec,ivParameterSpec);
		//加密数据
		byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
		//对加密后的数据使用Base64编码
		return Base64.getEncoder().encodeToString(encryptedData);
	}

	/**
	 * symEncrypt 对称加密
	 *
	 * @param strkey
	 *            对称密钥
	 * @param src
	 *            原文
	 * @return 密文
	 */
	public static String symEncrypt(String strkey, String src) throws Exception {
		String target = null;
		try {
			Key key = KeysFactory.getSymKey(strkey);
			// 加密
			Cipher cipher = Cipher.getInstance(ALGORITHM_AES);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encodeResult = cipher.doFinal(src.getBytes(StandardCharsets.UTF_8));
			target = (new BASE64Encoder()).encodeBuffer(encodeResult);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
			throw new Exception("加密失败" + e.getMessage());
		}
		return target;
	}

	/**
	 * pubEncrypt 公钥加密
	 *
	 * @param pubKey
	 *            公钥
	 * @param src
	 *            原文
	 * @return 密文
	 * @throws IOException
	 * @throws Exception
	 */
	public static String pubEncrypt(String pubKey, String src) throws Exception {
		String target = null;
		ByteArrayOutputStream out = null;
		try {
			Key key = KeysFactory.getPublicKey(pubKey);

			Cipher cipher = Cipher.getInstance(ALGORITHM_RSA);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			// encodeResult = cipher.doFinal(src.getBytes());
			byte[] data = src.getBytes();
			int inputLen = data.length;
			out = new ByteArrayOutputStream();
			int offSet = 0;
			byte[] cache;
			int i = 0;
			// 对数据分段加密
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
					cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * MAX_ENCRYPT_BLOCK;
			}

			target = (new BASE64Encoder()).encodeBuffer(out.toByteArray());
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException  e) {
			e.printStackTrace();
			throw new Exception("加密失败" + e.getMessage());
		}finally{
			if(out != null){
				out.close();
			}
		}
		return target;
	}

	/**
	 * base64编码
	 * @param v
	 * @return
	 */
	public static String base64Encode(String v){
		return Base64Utils.encodeToString(v.getBytes());
	}

}
