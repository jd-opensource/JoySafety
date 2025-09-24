package com.jd.security.llmsec.core.util;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class SignUtilTest {
    private final static String ENCODING = "UTF-8";
    private static final String MAC_NAME = "HmacSHA1";

    private static String hmacSHA1Encrypt(String plain, String secretKeyStr) {
        try {
            byte[] ketBytes = secretKeyStr.getBytes(ENCODING);
            SecretKey secretKey = new SecretKeySpec(ketBytes, MAC_NAME);
            Mac mac = Mac.getInstance(MAC_NAME);
            mac.init(secretKey);
            byte[] plainBytes = plain.getBytes(ENCODING);
            System.out.println("plainBytes = ");
            for (byte b : plainBytes) {
                System.out.printf("%02x", b & 0xFF);
            }

            System.out.println();

            byte[] macBytes = mac.doFinal(plainBytes);
            System.out.println("macBytes = ");
            for (byte b : macBytes) {
                System.out.printf("%02x", b & 0xFF);
            }
            return new String(Base64.getEncoder().encode(mac.doFinal(macBytes)));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        hmacSHA1Encrypt("你好", "1234abcd");
    }
}
