package com.jd.security.llmsec.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;



public class JsonUtils {
    public static <T> T obj2obj(Object obj, Class<T> t) {
        return JSON.parseObject(JSON.toJSONString(obj), t);
    }

    public static <T> T obj2obj(Object obj, TypeReference<T> t) {
        return JSON.parseObject(JSON.toJSONString(obj), t);
    }
}
