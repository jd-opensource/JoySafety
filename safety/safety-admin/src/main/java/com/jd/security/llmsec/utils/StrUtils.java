package com.jd.security.llmsec.utils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class StrUtils {
    private final static Pattern pattern = Pattern.compile("^\\d+");
    public static Long leadingNum(String str) {
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return Long.parseLong(matcher.group());
        } else {
            return null;
        }
    }
}
