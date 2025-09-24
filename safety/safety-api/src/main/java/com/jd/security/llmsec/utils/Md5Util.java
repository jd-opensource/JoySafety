package com.jd.security.llmsec.utils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;



public class Md5Util {
    public static String digest(String input) {
        return DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
    }
}
