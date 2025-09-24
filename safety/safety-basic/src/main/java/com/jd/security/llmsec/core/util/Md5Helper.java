package com.jd.security.llmsec.core.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Md5Helper {

    public static String md5Hex(String input) {
        try {
            byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(input.getBytes(StandardCharsets.UTF_8));
            return new String(encodeHex(md5Bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static final char[] HEX_CHARS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static char[] encodeHex(byte[] bytes) {
        char chars[] = new char[32];
        for (int i = 0; i < chars.length; i = i + 2) {
            byte b = bytes[i / 2];
            chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
            chars[i + 1] = HEX_CHARS[b & 0xf];
        }
        return chars;
    }

    public static void main(String[] args) {
        System.out.println(md5Hex("你好"));
    }
}
