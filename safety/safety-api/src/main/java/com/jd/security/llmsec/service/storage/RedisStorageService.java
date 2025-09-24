package com.jd.security.llmsec.service.storage;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.jd.security.llmsec.config.GlobalConf;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;



@Service
public class RedisStorageService implements StorageService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${session.redis.host}")
    private String redisHost;

    @Value("${session.redis.port}")
    private Integer redisPort;

    @Value("${session.redis.password}")
    private String redisPassword;
    /*
    https://github.com/redis/jedis/blob/master/src/test/java/redis/clients/jedis/JedisPoolTest.java
     */

    private JedisPool pool;
    @PostConstruct
    private void init() {
        // todo 配置放到外面
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(512);
        config.setMaxIdle(256);
        config.setMinIdle(32);
        pool = new JedisPool(config, redisHost, redisPort, 2000, redisPassword, 0);
    }

    @Override
    public Object execute(Callback callback) throws Exception {
        try {
            try (Jedis jedis = pool.getResource()) {
                return callback.call(jedis);
            }
        } catch (Exception e) {
            logger.error("执行redis命令失败", e);
            throw e;
        }
    }

    @Override
    public void lpush(String key, Object object) throws Exception {
        this.execute(jedis -> {
            String value = object instanceof String ? (String) object : JSON.toJSONString(object);
            return jedis.lpush(key, value);
        });
    }

    @Override
    public <T> T rpop(String key, Class<T> clazz) throws Exception {
        String rawValue = (String) this.execute(jedis -> jedis.rpop(key));
        return clazz == String.class ? (T)rawValue : JSON.parseObject(rawValue, clazz);
    }

    @Override
    public <T> List<T> rpopN(String key, int n, Class<T> clazz) throws Exception {
        List<Response<String>> rawValue = (List<Response<String>>)this.execute(jedis -> {
            Pipeline pipelined = jedis.pipelined();
            List<Response<String>> responses = Lists.newArrayList();
            for (int i = 0; i < n; i++) {
                Response<String> response = pipelined.rpop(key);
                responses.add(0, response);
            }

            pipelined.sync();
            return responses;
        });

        List<T> result = Lists.newArrayList();
        for (Response<String> response : rawValue) {
            String value = response.get();
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            result.add(clazz == String.class ? (T)value : JSON.parseObject(value, clazz));
        }
        result = Lists.reverse(result);
        return result;
    }

    @Override
    public boolean lock(String key, int expireSeconds) throws Exception {
        String value = GlobalConf.lockId();

        Long rawValue = (Long) this.execute(jedis -> jedis.setnx(key, value));
        boolean returnOk = Objects.equals(1L, rawValue);
        if (returnOk) {
            this.execute(jedis -> jedis.expire(key, expireSeconds));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void unlock(String key) throws Exception {
        this.execute(jedis -> jedis.del(key));
    }

    @Override
    public <T> List<T> lrange(String key, int start, int end, Class<T> clazz) throws Exception {
        List<String> rawStrList = (List<String>)this.execute(jedis -> jedis.lrange(key, start, end));
        List<T> result = Lists.newArrayList();
        for (String s : rawStrList) {
            if (clazz == String.class) {
                result.add((T)s);
            } else {
                result.add(JSON.parseObject(s, clazz));
            }
        }
        return result;
    }

    @Override
    public int llen(String key) throws Exception {
        Long len = (Long) this.execute(jedis -> jedis.llen(key));
        return len.intValue();
    }
}
