package com.jd.security.llmsec.service;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Maps;
import com.jd.security.llmsec.core.BusinessConf;
import com.jd.security.llmsec.core.api.defense.AB;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseResultFetchRequest;
import com.jd.security.llmsec.core.conf.ConfigResponse;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.conf.NodeConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;



public interface ConfigService {
    List<BusinessConf> businessConf();

    BusinessConf businessConfByName(String name);

    FunctionConfig funConfByName(String name);

    BusinessConf businessConf(DefenseApiRequest request);

    BusinessConf businessConf(DefenseResultFetchRequest request);

    default String mergeAccessKey(String accessKey, JSONObject ext) {
        if (ext == null || ext.isEmpty()) {
            return accessKey;
        }

        String subBizId = ext.getString("subBizId");
        if (StringUtils.isEmpty(subBizId)) {
            return accessKey;
        }
        return accessKey + "$" + subBizId;
    }

    BusinessConf businessConfByAccessKey(String key);

    Map<String, FunctionConfig> functionConfMap = Maps.newConcurrentMap();

    List<NodeConfig> functionChainConf(String name);

    void registerConfigChangeListener(Runnable runnable);

    ConfigResponse businessConfByName(String businessName, AB ab);
}
