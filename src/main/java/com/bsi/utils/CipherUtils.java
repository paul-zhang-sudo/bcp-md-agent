package com.bsi.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.message.AuthException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @author ovenhao
 * @version 1.0.0
 * @ClassName CipherUtils
 * @Description TODO
 * @createTime 2022/10/27 9:46
 */
@Slf4j
public class CipherUtils {
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";

    private static final int GCM_TAG_LENGTH = 16;

    private static final int GCM_IV_LENGTH = 12;

    private static final char IV_SEP = ':';

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static boolean verify(String data, String sign, PublicKey publicKey) {
        try {
            Signature cipher = Signature.getInstance("SHA256withRSA/PSS");
            cipher.initVerify(publicKey);
            cipher.update(data.getBytes(StandardCharsets.UTF_8));
            return cipher.verify(HexUtils.fromHexString(sign));
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            log.error("verify signature fail", e);
            return false;
        }
    }

    public static String sign(String data, PrivateKey privateKey) throws AuthException {
        try {
            Signature cipher = Signature.getInstance("SHA256withRSA/PSS");
            cipher.initSign(privateKey);
            cipher.update(data.getBytes(StandardCharsets.UTF_8));
            return HexUtils.toHexString(cipher.sign());
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            log.error("signature fail", e);
            throw new AuthException("sign fail");
        }
    }

    public static String decrypt(String data, String secret) throws AuthException {
        int idx = data.indexOf(IV_SEP);
        byte[] vector = HexUtils.fromHexString(data.substring(0, idx));
        byte[] encrypt = HexUtils.fromHexString(data.substring(idx + 1));

        SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, vector);
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            return new String(cipher.doFinal(encrypt, 0, encrypt.length), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("decrypt fail", e);
            throw new AuthException("decrypt body fail");
        }
    }

    public static String encrypt(String data, String secret) throws AuthException {
        byte[] vector = new byte[GCM_IV_LENGTH];
        (new SecureRandom()).nextBytes(vector);

        SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, vector);
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            // 将随机IV和密文拼接起来
            return HexUtils.toHexString(vector) + IV_SEP + HexUtils.toHexString(encrypted);
        } catch (Exception e) {
            log.error("encrypt fail", e);
            throw new AuthException("encrypt fail");
        }
    }

    public static X509Certificate loadCER(InputStream stream) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(stream);
    }
    //签名方法
    public static String sign(String data, String path, String type, String password) throws Exception {
        InputStream is = new FileInputStream(path);
        PrivateKey privateKey = loadPrivateKey(is, type, password);
        String sign = sign(data, privateKey);
        return sign;
    }


    /**
     * 加载私钥
     *
     * @param stream 流
     * @param type 类型.pem或.der
     * @return 私钥
     * @throws Exception 异常
     */
    public static PrivateKey loadPrivateKey(InputStream stream, String type, String password) throws Exception {
        byte[] content;
        if (type.equals("pem")) {
            return loadPem(stream, password);
        } else if (type.equals("der")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 8);
            IOUtils.copy(stream, out);
            content = out.toByteArray();
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(content);
            return kf.generatePrivate(keySpec);
        } else {
            throw new IllegalArgumentException("invalid private type " + type);
        }
    }

    private static PrivateKey loadPem(InputStream stream, String password) throws IOException {
        PEMParser pemParser = new PEMParser(new InputStreamReader(stream));
        Object object = pemParser.readObject();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        KeyPair kp;
        if (object instanceof PEMEncryptedKeyPair) {
            PEMEncryptedKeyPair ckp = (PEMEncryptedKeyPair) object;
            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
            kp = converter.getKeyPair(ckp.decryptKeyPair(decProv));
        } else {
            PEMKeyPair ukp = (PEMKeyPair) object;
            kp = converter.getKeyPair(ukp);
        }
        return kp.getPrivate();
    }
}
