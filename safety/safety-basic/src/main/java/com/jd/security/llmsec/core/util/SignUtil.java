package com.jd.security.llmsec.core.util;

import com.jd.security.llmsec.core.auth.AuthControlConf;
import com.jd.security.llmsec.core.auth.AuthRequest;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;


public class SignUtil {
    private final static Logger logger = LoggerFactory.getLogger(SignUtil.class);
    private final static String ENCODING = "UTF-8";
    private static final String MAC_NAME = "HmacSHA1";

    @Data
    public static class SignInfo {
        private String url;
        private String clientToken;
        private long timestamp;
        private String clientTokenKey;
        private String signature;
    }

    public static String sign(AuthControlConf authConf, AuthRequest authRequest) {
        StringBuilder plainBuilder = new StringBuilder();
        plainBuilder.append("accessKey=")
                .append(authConf.getAccessKey())
                .append("&")
                .append("accessTarget=")
                .append(authRequest.getAccessTarget())
                .append("&")
                .append("requestId=")
                .append(authRequest.getRequestId())
                .append("&")
                .append("timestamp=")
                .append(authRequest.getTimestamp())
                .append("&")
                .append("plainText=")
                .append(authRequest.getPlainText());
        return hmacSHA1Encrypt(plainBuilder.toString(), authConf.getSecretKey());
    }

    public final static int EXPIRE_MILISECONDS = 60 * 1000;

    public static boolean verify(AuthControlConf authConf, AuthRequest authRequest) {
        String mySignature = sign(authConf, authRequest);
        return Objects.equals(mySignature, authRequest.getSignature());
    }

    private static String hmacSHA1Encrypt(String plain, String secretKeyStr) {
        try {
            byte[] ketBytes = secretKeyStr.getBytes(ENCODING);
            SecretKey secretKey = new SecretKeySpec(ketBytes, MAC_NAME);
            Mac mac = Mac.getInstance(MAC_NAME);
            mac.init(secretKey);
            byte[] plainBytes = plain.getBytes(ENCODING);
            return new String(Base64.getEncoder().encode(mac.doFinal(plainBytes)));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
