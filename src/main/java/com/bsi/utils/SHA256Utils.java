package com.bsi.utils;

import org.apache.commons.codec.binary.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Utils {

	public static String getSHA256(String str,String digest) {
		MessageDigest messageDigest;
		String encodestr = "";
		try {
			messageDigest = MessageDigest.getInstance(digest);
			messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
			encodestr = byte2Hex(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return encodestr;
	}

	public static String getSHA256(String str) {
		return getSHA256(str,"SHA-256");
	}

	private static String byte2Hex(byte[] bytes) {
		StringBuffer stringBuffer = new StringBuffer();
		String temp = null;
		for (int i = 0; i < bytes.length; i++) {
			temp = Integer.toHexString(bytes[i] & 0xFF);
			if (temp.length() == 1) {
				stringBuffer.append("0");
			}
			stringBuffer.append(temp);
		}
		return stringBuffer.toString();
	}

	/**
	 * 加密传入的字符串
	 *
	 * @param key  密钥key
	 * @param body 加密字符串
	 * @return 加密结果
	 */
	public static String generateSignature(String key, String body)
		throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException {
		return base_64(hmacSHA256(key, body));
	}

	/**
	 * hamcSHA256加密算法
	 *
	 * @param macKey  秘钥key
	 * @param macData 加密内容-响应消息体
	 * @return 加密密文
	 */
	private static byte[] hmacSHA256(String macKey, String macData)
		throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException {
		SecretKeySpec secret = new SecretKeySpec(macKey.getBytes(), "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(secret);
		return mac.doFinal(macData.getBytes(StandardCharsets.ISO_8859_1));
	}

	/**
	 * 字节数组转字符串
	 *
	 * @param bytes 字节数组
	 * @return 字符串
	 */
	private static String base_64(byte[] bytes) {
		return new String(Base64.encodeBase64(bytes));
	}
}
