package com.bsi.utils;

import com.bsi.factory.Base64Util;
import com.bsi.factory.KeysFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.Base64Utils;
import sun.misc.BASE64Encoder;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * 加密类
 */
public class EncryptUtils {

	// RSA最大加密明文大小
	private static final int MAX_ENCRYPT_BLOCK = 117;
	private static final String ALGORITHM_AES = "AES";
	private static final String ALGORITHM_RSA = "RSA";

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
