package com.jd.security.llmsec.core.auth;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;

import java.util.Map;
import java.util.Set;


@Data
public class AuthControlConf {
    /**
     * 被访问的目标
     */
    private Set<String> accessTargets = Sets.newHashSet();

    /**
     * 访问者标识
     */
    private String accessKey;

    /**
     * 访问者私钥
     */
    private String secretKey;

    /**
     * 签名过期时间
     */
    private int expireSeconds = 300;

    /**
     * 限制请求的qps，以accessTarget的维度限制
     */
    private Map<String,Integer> qpsLimits = Maps.newHashMap();

    /**
     * 总限制
     */
    private Integer qpsLimit = 10;
}
