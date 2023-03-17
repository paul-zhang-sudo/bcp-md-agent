package com.bsi.utils;

import com.bsi.framework.core.utils.ExceptionUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Enumeration;

/**
 * 签名工具类
 */
public class SignatureUtils {

    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    /**
     * sm3加密
     * @param body
     * @return String
     */
    public static String sm3Signature(String body) {
        try {
            SM3Digest sm3Digest = new SM3Digest();
            sm3Digest.update(body.getBytes("UTF-8"), 0, body.getBytes("UTF-8").length);
            byte[] ret = new byte[sm3Digest.getDigestSize()];
            sm3Digest.doFinal(ret, 0);
            return Hex.toHexString(ret);
        } catch (Exception e) {
            info_log.error("sm3签名计算出现异常,异常信息：{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

    public static String sm2Signature(String privateKeyStr, String dataStr) throws CryptoException, IOException {
        byte[] key = Hex.decode(privateKeyStr);
        byte[] data = dataStr.getBytes("UTF-8");
        ECNamedCurveParameterSpec eCNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("sm2p256v1");
        ECDomainParameters domainParameters = new ECDomainParameters(eCNamedCurveParameterSpec.getCurve(), eCNamedCurveParameterSpec.getG(), eCNamedCurveParameterSpec.getN(), eCNamedCurveParameterSpec.getH(), eCNamedCurveParameterSpec.getSeed());
        ECPrivateKeyParameters privateKey = new ECPrivateKeyParameters(new BigInteger(1, key), domainParameters);
        ParametersWithID parameters = new ParametersWithID((CipherParameters) privateKey, "1234567812345678".getBytes());
        SM2Signer signer = new SM2Signer();
        signer.init(true, (CipherParameters)parameters);
        signer.update(data, 0, data.length);
        try{
            return Hex.toHexString(decodeDERSignature(signer.generateSignature()));
        }catch (Exception e){
            info_log.error("sm2签名计算出现异常,异常信息：{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

    private static byte[] decodeDERSignature(byte[] signature) throws IOException {
        ASN1InputStream stream = new ASN1InputStream(new ByteArrayInputStream(signature));
        ASN1Sequence primitive = (ASN1Sequence)stream.readObject();
        Enumeration<ASN1Integer> enumeration = primitive.getObjects();
        BigInteger R = ((ASN1Integer)enumeration.nextElement()).getValue();
        BigInteger S = ((ASN1Integer)enumeration.nextElement()).getValue();
        byte[] bytes = new byte[64];
        byte[] r = format(R.toByteArray());
        byte[] s = format(S.toByteArray());
        System.arraycopy(r, 0, bytes, 0, 32);
        System.arraycopy(s, 0, bytes, 32, 32);
        return bytes;
    }

    private static byte[] format(byte[] value) {
        if (value.length == 32)
            return value;
        byte[] bytes = new byte[32];
        if (value.length > 32) {
            System.arraycopy(value, value.length - 32, bytes, 0, 32);
        } else {
            System.arraycopy(value, 0, bytes, 32 - value.length, value.length);
        }
        return bytes;
    }
}
