package com.jd.security.llmsec.service.execute;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.jd.security.llmsec.config.GlobalConf;
import com.jd.security.llmsec.core.BusinessConf;
import com.jd.security.llmsec.core.api.defense.AB;
import com.jd.security.llmsec.core.conf.ConfigResponse;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.conf.NodeConfig;
import com.jd.security.llmsec.core.conf.RouterConfig;
import com.jd.security.llmsec.core.engine.FunctionChainBuilder;
import com.jd.security.llmsec.core.engine.Node;
import com.jd.security.llmsec.core.engine.Router;
import com.jd.security.llmsec.core.engine.routers.AbstractRouter;
import com.jd.security.llmsec.service.ConfigService;
import com.jd.security.llmsec.service.MonitorAlarmService;
import com.jd.security.llmsec.utils.FunctionUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.*;



@Service
public class FunctionChainBuilderImpl implements FunctionChainBuilder {
    private Logger logger = LoggerFactory.getLogger(FunctionChainBuilderImpl.class);
    @Autowired
    @Qualifier("DynamicConfigService")
    private ConfigService configService;

    @Autowired
    private GlobalConf globalConf;

    private Map<String, Node> rootMap = Maps.newHashMap();
    @PostConstruct
    private void init() {
        loadConf();
        configService.registerConfigChangeListener(this::loadConf);
    }

    private void loadConf() {
        long start = System.currentTimeMillis();
        List<BusinessConf> businessConfs = configService.businessConf();
        for (BusinessConf conf : businessConfs) {
            List<NodeConfig> nodeConfs = configService.functionChainConf(conf.getName());
            if (CollectionUtils.isEmpty(nodeConfs)) {
                logger.error("nodeconf不存在，businessConf={}", JSON.toJSONString(conf));
                MonitorAlarmService.basicIncr("no_node_conf_" + conf.getName());
                continue;
            }
            try {
                rootMap.put(conf.getName(), build(conf, nodeConfs));
            } catch (Exception e) {
                logger.error("构建chain失败, businessConf={}, nodeconf={}", JSON.toJSONString(conf), JSON.toJSONString(nodeConfs), e);
                MonitorAlarmService.basicIncr("error_node_conf_" + conf.getName());
            }
        }
        logger.info("加载配置结束，cost={}ms", System.currentTimeMillis() - start);
    }

    private Node build(BusinessConf businessConf, List<NodeConfig> nodeConfs) throws Exception {
        Map<String, Node> nodeMap = Maps.newHashMap();
        Node root = null;
        for (NodeConfig conf : nodeConfs) {
//            conf.setConfBaseDir(globalConf.confBaseDir());
            Node node = new Node(conf);
            if (Objects.equals(businessConf.getChainRootId(), conf.getNodeId())) {
                root = node;
            }
            if (nodeMap.containsKey(conf.getNodeId())) {
                throw new Exception(String.format("nodeId=%s重复了", conf.getNodeId()));
            }
            nodeMap.put(conf.getNodeId(), node);
        }

        ArrayList<Node> nodes = new ArrayList<>(nodeMap.values());
        for (Node node : nodes) {
            FunctionConfig funcConf = node.getConf().getFunctionConf();
//            funcConf.setConfBaseDir(globalConf.confBaseDir());
            funcConf = FunctionUtil.doMerge(funcConf);
            node.getConf().setFunctionConf(funcConf);
            node.setFunction(FunctionUtil.buildFunction(funcConf));
            node.setRouter(buildRouter(node.getConf().getRouterConf(), nodeMap));
        }

        return root;
    }

    private Router buildRouter(RouterConfig routerConf, Map<String, Node> nodeMap) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Preconditions.checkNotNull(routerConf.getType());
        Preconditions.checkNotNull(routerConf.getName());
        Class<? extends AbstractRouter> routerClazz = Router.ROUTER_MAP.get(routerConf.getType());
        return (Router)routerClazz.getDeclaredConstructors()[0].newInstance(routerConf, nodeMap);
    }

    @Override
    public Node root(String businessName, AB ab) {
        if (ab == null || ab.getDagVersion() == null || ab.getDagVersion() <= 0) {
            return rootMap.get(businessName);
        } else {
            ConfigResponse configResponse = configService.businessConfByName(businessName, ab);
            if (configResponse == null || CollectionUtils.isEmpty(configResponse.getConfs())) {
                throw new RuntimeException("不存在dagVersion=" + ab.getDagVersion());
            }
            ConfigResponse.Item item = configResponse.getConfs().get(0);
            try {
                return build(item.getBusinessConf(), item.getNodeConfigs());
            } catch (Exception e) {
                logger.error("构建dag失败, ab={}, conf={}", JSON.toJSONString(ab), JSON.toJSONString(item), e);
                throw new RuntimeException("构建dag失败" + e.getMessage());
            }
        }
    }

    @Override
    public Node refresh(String businessName) throws Exception {
        BusinessConf businessConf = configService.businessConfByName(businessName);
        if (businessConf == null) {
            return null;
        }
        List<NodeConfig> conf = conf(businessName);
        if (CollectionUtils.isEmpty(conf)) {
            return null;
        }
        Node node = build(businessConf, conf);
        this.rootMap.put(businessName, node);
        return node;
    }

    @Override
    public List<NodeConfig> conf(String businessName) {
        return configService.functionChainConf(businessName);
    }
}
