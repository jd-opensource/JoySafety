package com.jd.security.llmsec.service.execute.functions;

import com.google.common.collect.Maps;
import com.jd.security.llmsec.core.check.RiskCheckType;

import java.util.Map;



public class FunctionResourcePool {
    private static Map<String, Object> pool = Maps.newConcurrentMap();
    public synchronized static Object getFunc(RiskCheckType type, String id) {
        return pool.get(type + "$" + id);
    }

    // 如果已有了，返回已有的；如果没有，返回当前传入的
    public synchronized static Object addFuncIfAbsent(RiskCheckType type, String id, Object resource) {
        String key = type + "$" + id;
        Object oldFunc = pool.get(key);
        if (oldFunc != null) {
            return oldFunc;
        } else {
            pool.put(key, resource);
            return resource;
        }
    }
}
