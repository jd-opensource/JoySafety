package com.jd.security.llmsec.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.security.llmsec.core.BusinessConf;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.core.api.defense.AB;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseResultFetchRequest;
import com.jd.security.llmsec.core.conf.ConfigReq;
import com.jd.security.llmsec.core.conf.ConfigResponse;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.conf.NodeConfig;
import com.jd.security.llmsec.utils.HttpHelper;
import okhttp3.OkHttpClient;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;



@Service("DynamicConfigService")
public class DynamicConfigService implements ConfigService{
    private Logger logger = LoggerFactory.getLogger(DynamicConfigService.class);

    @Value("${conf.dynamic.group}")
    private String group;
    @Value("${conf.dynamic.url}")
    private String configUrl;

    private volatile Map<String, BusinessConf> name2BusinessConf = Maps.newHashMap();
    private volatile Map<String, List<NodeConfig>> nodeConfMap = Maps.newHashMap();
    private volatile long lastUpdateTime = -1;

    @PostConstruct
    private void init() throws IOException {
        loadConf();
    }


    private OkHttpClient client = new OkHttpClient.Builder().callTimeout(10, TimeUnit.SECONDS).build();
    private volatile ConfigResponse preDynamicConf = null;
    @Scheduled(fixedRate = 60000, initialDelay = 60000)
    private void loadConf() throws IOException {
        long start = System.currentTimeMillis();
        Map<String, BusinessConf> name2BusinessConf = Maps.newHashMap();
        Map<String, List<NodeConfig>> nodeConfMap = Maps.newHashMap();

        ConfigResponse dynamicConf = loadDynamic(name2BusinessConf, nodeConfMap);
        boolean shouldUpdate = shouldUpdate(preDynamicConf, dynamicConf);
        if (!shouldUpdate) {
            logger.info("配置无变更");
            return;
        }
        preDynamicConf = dynamicConf;
        if (dynamicConf != null) {
            if (CollectionUtils.isNotEmpty(dynamicConf.getConfs())) {
                // 需要更新时全量更新
                List<ConfigResponse.Item> items = dynamicConf.getConfs();
                items.forEach(x -> {
                    String name = x.getBusinessConf().getName();
                    name2BusinessConf.put(name, x.getBusinessConf());
                    nodeConfMap.put(name, x.getNodeConfigs());
                });
            }
        }

        if (name2BusinessConf.isEmpty() || nodeConfMap.isEmpty()) {
            logger.warn("配置更新失败，business={}, node={}, cost={}ms", JSON.toJSONString(name2BusinessConf), JSON.toJSONString(nodeConfMap), System.currentTimeMillis() - start);
            return;
        }
        loadFunctions(dynamicConf);

        this.nodeConfMap = nodeConfMap;
        this.name2BusinessConf =  name2BusinessConf;
        this.lastUpdateTime = System.currentTimeMillis();

        logger.info("配置更新成功，business={}, function={}, cost={}ms", JSON.toJSONString(name2BusinessConf),
                JSON.toJSONString(ConfigService.functionConfMap),
                System.currentTimeMillis() - start);
        for (String biz : nodeConfMap.keySet()) {
            logger.info("配置更新成功，business={}, node={}", biz, JSON.toJSONString(nodeConfMap.get(biz)));
        }
        if (CollectionUtils.isNotEmpty(this.listeners)) {
            for (Runnable run : this.listeners) {
                pool.submit(run);
            }
        }
    }

    private boolean shouldUpdate(ConfigResponse preDynamicConf, ConfigResponse dynamicConf) {
        if (preDynamicConf == null) { // 初次加载
            return true;
        } else if (dynamicConf == null || CollectionUtils.isEmpty(dynamicConf.getConfs())) {
            // 动态配置为空
            // 防止出现异常时丢失配置（在动态配置服务侧至少保留一个可用配置）
            return false;
        } else {
            if (CollectionUtils.isEmpty(preDynamicConf.getConfs())) {
                return true;
            }

            if (preDynamicConf.getConfs().size() != dynamicConf.getConfs().size()) {
                return true;
            }
            Map<String, ConfigResponse.Item> preMap = Maps.newHashMap();
            Map<String, ConfigResponse.Item> newMap = Maps.newHashMap();
            preDynamicConf.getConfs().forEach(x -> {
                preMap.put(x.getBusinessConf().getName(), x);
            });
            dynamicConf.getConfs().forEach(x -> {
                newMap.put(x.getBusinessConf().getName(), x);
            });

            for (String name : newMap.keySet()) {
                if (!preMap.containsKey(name)) {
                    return true;
                }

                Long preLastChangeTimestamp = preMap.get(name).getLastChangeTimestamp();
                Long nowLastChangeTimestamp = newMap.get(name).getLastChangeTimestamp();
                if (!Objects.equals(preLastChangeTimestamp, nowLastChangeTimestamp)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static void loadFunctions(ConfigResponse dynamicConf) {
        if (dynamicConf == null) {
            return;
        }
        Map<String, FunctionConfig> dynamicFuncMap = dynamicConf.getFunctionMap();
        if (dynamicFuncMap == null || dynamicFuncMap.isEmpty()) {
            return;
        }

        Map<String, FunctionConfig> allFuncMap = Maps.newHashMap();
        allFuncMap.putAll(dynamicFuncMap);

        for (String key : ConfigService.functionConfMap.keySet()) {
            if (!allFuncMap.containsKey(key)) {
                allFuncMap.put(key, ConfigService.functionConfMap.get(key));
            }
        }

        ConfigService.functionConfMap.clear();
        ConfigService.functionConfMap.putAll(allFuncMap);
    }

    private @Nullable ConfigResponse loadDynamic(Map<String, BusinessConf> name2BusinessConf, Map<String, List<NodeConfig>> nodeConfMap) throws IOException {
        ConfigReq request = new ConfigReq();
        request.setGroup(group);
        ResponseMessage<ConfigResponse> responseMessage = HttpHelper.post(client, configUrl, HttpHelper.keepaliveHeader(), request, new TypeReference<ResponseMessage<ConfigResponse>>() {
        });

        if (responseMessage == null || !responseMessage.success()) {
            logger.error("获取动态配置失败, response={}", JSON.toJSONString(responseMessage));
            // todo 加报警
            throw new RuntimeException("获取动态配置失败, error=" + responseMessage.getMessage());
        }

        if (responseMessage.getData() == null) {
            logger.error("获取动态配置失败, 配置为空");
            // todo 加报警
            return null;
        }
        return responseMessage.getData();
    }

    private final ExecutorService pool = Executors.newCachedThreadPool();


    @Override
    public List<BusinessConf> businessConf() {
        return Lists.newArrayList(this.name2BusinessConf.values().iterator());
    }

    @Override
    public BusinessConf businessConfByName(String name) {
        return this.name2BusinessConf.get(name) ;
    }

    @Override
    public FunctionConfig funConfByName(String name) {
        return ConfigService.functionConfMap.get(name);
    }

    @Override
    public BusinessConf businessConf(DefenseApiRequest request) {
        return getBusinessConf(request.getAccessKey(), request.getMessageInfo().getExt());
    }

    @Override
    public BusinessConf businessConf(DefenseResultFetchRequest request) {
        return getBusinessConf(request.getAccessKey(), request.getExt());
    }

    private BusinessConf getBusinessConf(String accessKey, JSONObject ext) {
        String mergedAccessKey = mergeAccessKey(accessKey, ext);
        BusinessConf ret = businessConfByAccessKey(mergedAccessKey);
        return ret == null ? businessConfByAccessKey(accessKey) : ret;
    }

    @Override
    public BusinessConf businessConfByAccessKey(String key) {
        return this.businessConfByName(key);
    }

    @Override
    public List<NodeConfig> functionChainConf(String name) {
        return this.nodeConfMap.get(name);
    }

    private final List<Runnable> listeners = Lists.newArrayList();
    @Override
    public void registerConfigChangeListener(Runnable runnable) {
        synchronized (listeners) {
            listeners.add(runnable);
        }
    }

    @Override
    public ConfigResponse businessConfByName(String businessName, AB ab) {
        ConfigReq request = new ConfigReq();
        request.setGroup(group);
        request.setBusinessName(businessName);
        request.setAb(ab);
        try {
            ResponseMessage<ConfigResponse> response = HttpHelper.post(client, configUrl, HttpHelper.keepaliveHeader(), request, new TypeReference<ResponseMessage<ConfigResponse>>() {
            });
            return response != null && response.success() ? response.getData() : null;
        } catch (IOException e) {
            logger.error("按版本获取配置失败");
            throw new RuntimeException("获取动态配置失败，" + e.getMessage());
        }
    }
}
