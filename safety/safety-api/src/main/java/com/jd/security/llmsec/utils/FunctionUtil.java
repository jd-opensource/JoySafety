package com.jd.security.llmsec.utils;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.engine.AbstractFunction;
import com.jd.security.llmsec.core.engine.Function;
import com.jd.security.llmsec.service.ConfigService;
import com.jd.security.llmsec.service.execute.FunctionRegistry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;


public class FunctionUtil {
    public static Function buildFunction(FunctionConfig functionConf) throws Exception {
        if (StringUtils.isNotEmpty(functionConf.getRef()) && StringUtils.isEmpty(functionConf.getName())) {
            functionConf = doMerge(functionConf);
        }

        Preconditions.checkNotNull(functionConf.getType());
        Preconditions.checkNotNull(functionConf.getName());
        Preconditions.checkArgument(functionConf.getTimeoutMilliseconds() != null && NumberUtils.compare(functionConf.getTimeoutMilliseconds(), 0) > 0, "timeoutMilliseconds必须要设置");
        Class<? extends AbstractFunction> funcClazz = FunctionRegistry.FUNC_MAP.get(functionConf.getType());
        return (Function)funcClazz.getDeclaredConstructors()[0].newInstance(functionConf);
    }


    public static FunctionConfig doMerge(FunctionConfig cur) throws Exception {
        if (StringUtils.isEmpty(cur.getRef())) {
            return cur;
        }

        FunctionConfig copyFrom = ConfigService.functionConfMap.get(cur.getRef());
        if (copyFrom == null) {
            throw new Exception("function ref 不存在: " + cur.getRef());
        }
        FunctionConfig copyTo = new FunctionConfig();
        copyTo.setRef(cur.getRef());
        copyProperties(copyFrom, copyTo);
        Map<String, Object> mergedConf = mergeProperties(copyTo.getConf(), cur.getConf());
        copyTo.setConf(mergedConf);
        return copyTo;
    }

    // custome来覆盖template，custome有的覆盖template，custome中没有的，用template
    // value为map时，递归
    private static Map<String, Object> mergeProperties(Map<String, Object> template, Map<String, Object> custome) {
        if (template == null) {
            return custome;
        }

        if (custome == null) {
            return template;
        }


        Map<String,Object> ret = Maps.newLinkedHashMap();
        Set<String> templateKeys = template.keySet();
        Set<String> customeKeys = custome.keySet();
        Set<String> inters = Sets.intersection(templateKeys, customeKeys).stream().collect(Collectors.toSet());

        Set<String> onlyTemplateKeys = setSub(templateKeys, inters);
        Set<String> onlyCustomeKeys = setSub(customeKeys, inters);

        if (CollectionUtils.isNotEmpty(onlyTemplateKeys)) {
            onlyTemplateKeys.forEach(x -> ret.put(x, template.get(x)));
        }

        if (CollectionUtils.isNotEmpty(onlyCustomeKeys)) {
            onlyCustomeKeys.forEach(x -> ret.put(x, custome.get(x)));
        }

        for (String k : inters) {
            Object o = template.get(k);
            if (o instanceof Map) {
                ret.put(k, mergeProperties((Map<String, Object>)template.get(k), (Map<String, Object>)custome.get(k)));
            } else {
                ret.put(k, custome.get(k));
            }
        }

        return ret;
    }

    // 集合相关
    private static Set<String> setSub(Set<String> templateKeys, Set<String> inters) {
        Set<String> ret = Sets.newHashSet();
        for (String k : templateKeys) {
            if (!inters.contains(k)) {
                ret.add(k);
            }
        }

        return ret;
    }

    private static void copyProperties(FunctionConfig copyFrom, FunctionConfig copyTo) {
        copyTo.setType(copyFrom.getType());
        copyTo.setTarget(copyFrom.getTarget());
        copyTo.setName(copyFrom.getName());
        copyTo.setDesc(copyFrom.getDesc());
        copyTo.setTimeoutMilliseconds(copyFrom.getTimeoutMilliseconds());
        copyTo.setConf(copy(copyFrom.getConf()));
    }

    private static Map<String, Object> copy(Map<String, Object> conf) {
        Map<String,Object> ret = Maps.newLinkedHashMap();
        if (conf == null || conf.isEmpty()) {
            return ret;
        }

        for (Map.Entry<String,Object> entry : conf.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                ret.put(key, copy((Map<String, Object>)value));
            } else if (value instanceof List) {
                List<Object> objList = Lists.newArrayList();
                for (Object obj : (List)value) {
                    if (obj instanceof Map) {
                        objList.add(copy((Map<String, Object>) obj));
                    } else {
                        objList.add(obj);
                    }
                }
                ret.put(key, objList);
            } else {
                ret.put(key, value);
            }
        }
        return ret;
    }

    public static void main(String[] args) {
        Map<String, Object> mock1 = mock();

        System.out.println(JSON.toJSONString(mock1));
//        Map<String,Object> copyTo  = copy(copyFrom);
//        System.out.println(JSON.toJSONString(copyTo));
        /*
{"a":10,"b1":[1,2,3],"b2":[[1,2,3],[1,2,3],[1,2,3]],"b3":[{"x1":1,"y1":2},{"x2":1,"y2":2}],"c":{"x3":1,"y3":2}}
{"a":10,"b1":[1,2,3],"b2":[[1,2,3],[1,2,3],[1,2,3]],"b3":[{"x1":1,"y1":2},{"x2":1,"y2":2}],"c":{"x3":1,"y3":2}}
         */

        Map<String, Object> mock2 = mock();
//        mock2.put("a", null);
        mock2.remove("a");
        mock2.put("b1", Arrays.asList(1,2,3,4,5,6));
        mock2.put("c",
                new LinkedHashMap(){{
                    put("x3", 1);
                    put("y3", 2);
                    put("z3", 3);
                }});

        Map<String,Object> merged  = mergeProperties(mock1, mock2);
        System.out.println(JSON.toJSONString(merged));
    }

    private static @NotNull Map<String, Object> mock() {
        Map<String,Object> copyFrom = Maps.newLinkedHashMap();

        copyFrom.put("a", 10);
        copyFrom.put("b1", Arrays.asList(1,2,3));
        copyFrom.put("b2", Arrays.asList(Arrays.asList(1,2,3),Arrays.asList(1,2,3),Arrays.asList(1,2,3)));
        copyFrom.put("b3", Arrays.asList(new LinkedHashMap(){{
            put("x1", 1);
            put("y1", 2);
        }},new LinkedHashMap(){{
                    put("x2", 1);
                    put("y2", 2);
                }}));

        copyFrom.put("c",
                new LinkedHashMap(){{
                    put("x3", 1);
                    put("y3", 2);
                }});
        return copyFrom;
    }
}
