package com.jd.security.llmsec.service.storage;

import com.alibaba.fastjson2.TypeReference;
import redis.clients.jedis.Jedis;

import java.util.List;



public interface StorageService {
    interface Callback {
        Object call(Jedis jedis);
    }

    Object execute(Callback callback) throws Exception;

    void lpush(String key, Object object) throws Exception;

    <T> T rpop(String key, Class<T> clazz) throws Exception;

    <T> List<T> rpopN(String key, int n, Class<T> clazz) throws Exception;

    boolean lock(String key, int expireSeconds) throws Exception;

    void unlock(String key) throws Exception;


    <T> List<T> lrange(String key, int start, int end, Class<T> clazz) throws Exception;


    int llen(String key) throws Exception;

}
