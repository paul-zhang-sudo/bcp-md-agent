package com.bsi.utils;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public class FinalShellDecodePass {
    public FinalShellDecodePass() {
    }

    public static void main(String[] args) throws Exception {
        String orgdecodePass = "GjBGXnM4V0bIFEtrvabvu+NhKJjq09e5";

        orgdecodePass = "XCRnBi48NR9RLJB3UD3Qo8ua+nWtiF9y"; //百威 BCP-WEB-root
        orgdecodePass = "eAFjFgtEPSkp67WVP9gJQ2UrJm6uXj6C"; //大湾区-一体机-root
        //orgdecodePass = "28c6ac195ecf82608d183b0c8537abe515669c3064862bed072c7aceb5a435a454e2eb8fdcf7c45b361652477de6695315802c45b57d8cd6e7";

        System.out.println("原始加密密文：" + orgdecodePass);
        System.out.println("解密后明文：" + decodePass(orgdecodePass));
    }

    public static byte[] desDecode(byte[] data, byte[] head) throws Exception {
        SecureRandom sr = new SecureRandom();
        DESKeySpec dks = new DESKeySpec(head);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey securekey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(2, securekey, sr);
        return cipher.doFinal(data);
    }

    public static String decodePass(String data) throws Exception {
        if (data == null) {
            return null;
        } else {
            String rs = "";
            byte[] buf = Base64.getDecoder().decode(data);
            byte[] head = new byte[8];
            System.arraycopy(buf, 0, head, 0, head.length);
            byte[] d = new byte[buf.length - head.length];
            System.arraycopy(buf, head.length, d, 0, d.length);
            byte[] bt = desDecode(d, ranDomKey(head));
            rs = new String(bt);
            return rs;
        }
    }

    static byte[] ranDomKey(byte[] head) {
        long ks = 3680984568597093857L / (long)(new Random((long)head[5])).nextInt(127);
        Random random = new Random(ks);
        int t = head[0];

        for(int i = 0; i < t; ++i) {
            random.nextLong();
        }

        long n = random.nextLong();
        Random r2 = new Random(n);
        long[] ld = new long[]{(long)head[4], r2.nextLong(), (long)head[7], (long)head[3], r2.nextLong(), (long)head[1], random.nextLong(), (long)head[2]};
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        long[] var15 = ld;
        int var14 = ld.length;

        for(int var13 = 0; var13 < var14; ++var13) {
            long l = var15[var13];

            try {
                dos.writeLong(l);
            } catch (IOException var18) {
                var18.printStackTrace();
            }
        }

        try {
            dos.close();
        } catch (IOException var17) {
            var17.printStackTrace();
        }

        byte[] keyData = bos.toByteArray();
        keyData = md5(keyData);
        return keyData;
    }

    public static byte[] md5(byte[] data) {
        String ret = null;
        byte[] res = null;

        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(data, 0, data.length);
            res = m.digest();
            ret = (new BigInteger(1, res)).toString(16);
        } catch (NoSuchAlgorithmException var4) {
            var4.printStackTrace();
        }

        return res;
    }
}
