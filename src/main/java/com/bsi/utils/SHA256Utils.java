package com.bsi.utils;

import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.StringUtils;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Utils {

	private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

	public static String getSHA256(String str,String digest) {
		MessageDigest messageDigest;
		String encodestr = "";
		try {
			messageDigest = MessageDigest.getInstance(digest);
			messageDigest.update(str.getBytes(StandardCharsets.UTF_8));
			encodestr = byte2Hex(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			info_log.info("getSHA256方法调用报错:{}", ExceptionUtils.getFullStackTrace(e));
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
	public static String generateSignature(String key, String body, String name)
		throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException {
		if(name == null){
			name = "HmacSHA256";
		}
		return base_64(hMac(key, body, name));
	}

	/**
	 * hamc加密算法
	 *
	 * @param macKey  秘钥key
	 * @param macData 加密内容-响应消息体
	 * @return 加密密文
	 */
	private static byte[] hMac(String macKey, String macData, String name)
			throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException {
		SecretKeySpec secret = new SecretKeySpec(macKey.getBytes(), name);
		Mac mac = Mac.getInstance(name);
		mac.init(secret);
		return mac.doFinal(macData.getBytes(StandardCharsets.ISO_8859_1));
	}

	/**
	 * 加密传入的字符串
	 *
	 * @param key  密钥key
	 * @param body 加密字符串
	 * @param name 加密方法名
	 * @return 加密结果
	 */
	public static String generateSignature_UTF(String key, String body, String name)
			throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException {
		return base_64(hMacMD5(key, body, name));
	}

	/**
	 * hamc加密算法
	 *
	 * @param macKey  秘钥key
	 * @param macData 加密内容-响应消息体
	 * @param name 加密方法名
	 * @return 加密密文
	 */
	private static byte[] hMacMD5(String macKey, String macData, String name)
			throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException {
		SecretKeySpec secret = new SecretKeySpec(macKey.getBytes(), name);
		Mac mac = Mac.getInstance(name);
		mac.init(secret);
		return mac.doFinal(macData.getBytes(StandardCharsets.UTF_8));
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

	public static byte[] base_64_decode_bytes(byte[] bytes) {
		return Base64.decodeBase64(bytes);
	}
	public static byte[] base_64_decode_bytes(String string) {
		return base_64_decode_bytes(string.getBytes());
	}
	public static byte[] toBytes(String str,String charset){
		return StringUtils.isEmpty(charset) ? str.getBytes():str.getBytes(Charset.forName(charset));
	}

	/**
	 * 将加密后的字节数组转换成字符串
	 *
	 * @param b 字节数组
	 * @return 字符串
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuilder hs = new StringBuilder();
		String stmp;
		for (int n = 0; b != null && n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0XFF);
			if (stmp.length() == 1){
				hs.append('0');
			}
			hs.append(stmp);
		}
		return hs.toString().toLowerCase();
	}

	/**
	 * 飞书消息推送签名方法
	 * @param secret
	 * @param timestamp
	 * @return
	 */
	public static String genSign(String secret, long timestamp) {
		//把timestamp+"\n"+密钥当做签名字符串
		String stringToSign = timestamp + "\n" + secret;
		String rs = "";
		//使用HmacSHA256算法计算签名
		try{
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] signData = mac.doFinal(new byte[]{});
			rs =  new String(Base64.encodeBase64(signData));
		}catch (Exception e){
			info_log.info("飞书签名方法调用失败，失败信息:{}", ExceptionUtils.getFullStackTrace(e));
		}
		return rs;
	}

	/**
	 * sha256_HMAC加密
	 *
	 * @param message 消息
	 * @param secret 秘钥
	 * @return 加密后字符串
	 */
	public static String hmacSHA256(String secret, String message) throws
			Exception {
		String hash = "";
		Mac hmacSha256 = Mac.getInstance("HmacSHA256");
		SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(),
				"HmacSHA256");
		hmacSha256.init(secretKey);
		byte[] bytes = hmacSha256.doFinal(message.getBytes());
		hash = byteArrayToHexString(bytes);
		return hash;
	}

	public static String generateSignatureFl(String appId, String appSecret, String serialId, Long timestamp)
			throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException {
		String plaintext = appId + timestamp + serialId;
		try {
			return hmacSHA256(appSecret, plaintext);
		} catch (Exception e) {
			info_log.info("理想签名方法调用失败，失败信息:{}", ExceptionUtils.getFullStackTrace(e));
		}
		return null;
	}
}
