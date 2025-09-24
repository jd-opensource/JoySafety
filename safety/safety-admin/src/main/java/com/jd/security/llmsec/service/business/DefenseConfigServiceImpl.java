package com.jd.security.llmsec.service.business;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jd.security.llmsec.contoller.business.DefenseConfigApiController;
import com.jd.security.llmsec.contoller.business.DefenseConfigApiController.FuncBizReq;
import com.jd.security.llmsec.core.BusinessConf;
import com.jd.security.llmsec.core.Finals;
import com.jd.security.llmsec.core.api.defense.AB;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.*;
import com.jd.security.llmsec.core.conf.funcs.ParallelConf;
import com.jd.security.llmsec.core.engine.routers.GroovyRouter;
import com.jd.security.llmsec.core.session.BusinessType;
import com.jd.security.llmsec.core.session.CheckConf;
import com.jd.security.llmsec.data.pojo.BusinessInfoWithBLOBs;
import com.jd.security.llmsec.data.pojo.FunctionConf;
import com.jd.security.llmsec.pojo.*;
import com.jd.security.llmsec.pojo.business.BusinessInfoVO;
import com.jd.security.llmsec.pojo.business.ExecuteDagConfVO;
import com.jd.security.llmsec.pojo.business.FunctionConfVO;
import com.jd.security.llmsec.utils.FileUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.jd.security.llmsec.pojo.business.BusinessInfoVO.ACCESS_TARGEST;



@Service
public class DefenseConfigServiceImpl implements DefenseConfigService {
    private Logger logger = LoggerFactory.getLogger(DefenseConfigServiceImpl.class);

    @Autowired
    @Qualifier("BusinessConfigServiceImpl")
    private BusinessConfigService businessConfigService;
    @Autowired
    private FunctionConfigService functionConfigService;
    @Autowired
    @Qualifier("DagConfigServiceImpl")
    private DagConfigService dagConfigService;
    private volatile Map<String, ConfData> onlineGroupedConf = Maps.newHashMap();
    private Map<String, ConfData> latestOfflinedGroupedConf = Maps.newHashMap();

    @PostConstruct
    private void init() {
        refreshConf();
    }

    @Scheduled(fixedRate = 60000)
    private void refreshConf() {
        long start = System.currentTimeMillis();
        logger.info("开始刷新配置数据");
        Map<String, ConfData> newGroupedConf = Maps.newHashMap();
        fillFunctions(newGroupedConf);

        List<BusinessInfoVO> bizConfs = businessConfigService.allActiveConf();
        if (CollectionUtils.isNotEmpty(bizConfs)) {
            try {
                for (BusinessInfoVO conf : bizConfs) {
                    String group = conf.getGroup();
                    if (StringUtils.isEmpty(group)) {
                        logger.error("group为空，conf={}", JSON.toJSONString(conf));
                        continue;
                    }

                    ConfData confData = newGroupedConf.get(group);
                    if (confData == null) {
                        confData = new ConfData();
                        newGroupedConf.put(group, confData);
                    }
                    confData.getBusiness().put(conf.getName(), conf);
                }

                // todo 同一个业务，多个配置的情况
                List<ExecuteDagConfVO> dagConfs = dagConfigService.allActiveConf();
                for (ExecuteDagConfVO conf : dagConfs) {
                    String group = conf.getGroup();
                    if (StringUtils.isEmpty(group)) {
                        logger.error("group为空，conf={}", JSON.toJSONString(conf));
                        continue;
                    }
                    String businessName = conf.getBusinessName();

                    ConfData confData = newGroupedConf.get(group);
                    if (confData == null) {
                        confData = new ConfData();
                        newGroupedConf.put(group, confData);
                    }
                    confData.getDags().put(businessName, conf);

                    List<NodeConfig> nodeConfigs = JSON.parseObject(conf.getConf(), new TypeReference<List<NodeConfig>>() {
                    });
                    Set<String> funcSet = Sets.newHashSet();
                    for (NodeConfig nodeConf : nodeConfigs) {
                        FunctionConfig functionConf = nodeConf.getFunctionConf();
                        addFuncs(functionConf, funcSet);
                    }
                    confData.getBiz2func().put(businessName, funcSet);

                    Map<String, Set<String>> func2biz = confData.getFunc2biz();
                    for (String func : funcSet) {
                        Set<String> bizs = func2biz.get(func);
                        if (bizs == null) {
                            bizs = Sets.newHashSet();
                            func2biz.put(func, bizs);
                        }
                        bizs.add(businessName);
                    }
                }
            } catch (Exception e) {
                logger.error("构建全量配置失败, cost={}ms", System.currentTimeMillis() - start, e);
                // todo 加报警
            }
        } else {
            logger.warn("无业务配置");
        }
        if (!newGroupedConf.isEmpty()) {
            this.onlineGroupedConf = newGroupedConf;
        }
        logger.info("结束刷新配置数据, cost={}ms", System.currentTimeMillis() - start);
    }

    private void fillFunctions(Map<String, ConfData> newGroupedConf) {
        List<FunctionConfVO> funcs = functionConfigService.allActiveConf();
        for (FunctionConfVO conf : funcs) {
            String group = conf.getGroup();
            if (StringUtils.isEmpty(group)) {
                logger.error("group为空，conf={}", JSON.toJSONString(conf));
                continue;
            }

            ConfData confData = newGroupedConf.get(group);
            if (confData == null) {
                confData = new ConfData();
                newGroupedConf.put(group, confData);
            }
            confData.getFunctions().put(conf.getName(), conf);
        }
    }

    private static void addFuncs(FunctionConfig functionConf, Set<String> funcs) {
        if (Objects.equal(RiskCheckType.parallel, functionConf.getType())) {
            ParallelConf parallelConf = JSON.parseObject(JSON.toJSONString(functionConf.getConf()), ParallelConf.class);
            for (FunctionConfig subConf : parallelConf.getFunctionConfs()) {
                addFuncs(subConf, funcs);
            }
        } else {
            if (StringUtils.isNotEmpty(functionConf.getRef())) {
                funcs.add(functionConf.getRef());
            }
        }
    }


    @Override
    public List<BusinessInfoVO> func2Biz(FuncBizReq req) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(req.getGroup()), "group不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(req.getFunctionName()), "functionName不能为空");
        ConfData confData = onlineGroupedConf.get(req.getGroup());
        if (confData == null) {
            return Lists.newArrayList();
        } else {
            Set<String> bizNames = confData.getFunc2biz().get(req.getFunctionName());
            return bizNames.stream().map(name -> confData.getBusiness().get(name)).collect(Collectors.toList());
        }
    }

    @Override
    public List<FunctionConfVO> biz2Func(FuncBizReq req) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(req.getGroup()), "group不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(req.getBusinessName()), "businessName不能为空");
        ConfData confData = onlineGroupedConf.get(req.getGroup());
        if (confData == null) {
            return Lists.newArrayList();
        } else {
            Set<String> functionsNames = confData.getBiz2func().get(req.getBusinessName());
            return functionsNames.stream().map(name -> confData.getFunctions().get(name)).collect(Collectors.toList());
        }
    }

    @Override
    public ConfigResponse configs(ConfigReq req) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(req.getGroup()), "group为空");
        ConfData confData = onlineGroupedConf.get(req.getGroup());
        if (confData == null) {
            logger.warn("分组无可用配置，group={}", req.getGroup());
            return null;
        }

        if (StringUtils.isNotEmpty(req.getBusinessName())) {
            ConfigResponse.Item item = buildSingleBusinessConf(confData, req.getBusinessName(), req);
            if (item == null) {
                return null;
            } else {
                ConfigResponse ret = new ConfigResponse();
                ret.getConfs().add(item);

                // 同步获取获取最新的function配置
                Map<String, ConfData> newGroupedConf = Maps.newHashMap();
                fillFunctions(newGroupedConf);

                ret.setFunctionMap(convert(newGroupedConf.get(req.getGroup()).getFunctions()));
                return ret;
            }
        } else {
            return buildAllBusinessConf(confData, req);
        }
    }

    private ConfigResponse buildAllBusinessConf(ConfData confData, ConfigReq req) {
        Set<String> allBusiness = confData.getBusiness().keySet();
        ConfigResponse ret = new ConfigResponse();
        for (String one : allBusiness) {
            try {
                ConfigResponse.Item item = buildSingleBusinessConf(confData, one, req);
                if (item != null) {
                    ret.getConfs().add(item);
                }
                ret.setFunctionMap(convert(confData.getFunctions()));
            } catch (Exception e) {
                logger.error("获取配置失败，business={}", one, e);
            }
        }

        return ret;
    }

    private ConfigResponse.Item buildSingleBusinessConf(ConfData confData, String businessName, ConfigReq req) {
        BusinessInfoVO businessInfoVO = confData.getBusiness().get(businessName);
        if (businessInfoVO == null) {
            logger.warn("应用无对应配置，businessName={}", businessName);
            return null;
        }

        ExecuteDagConfVO executeDagConf;
        AB ab = req.getAb();
        if (ab == null || ab.getDagVersion() == null || ab.getDagVersion() <= 0) {
            executeDagConf = confData.getDags().get(businessName);
        } else {
            executeDagConf = dagConfigService.getDag(req.getGroup(), req.getBusinessName(), ab.getDagVersion());
        }
        if (executeDagConf == null) {
            logger.warn("应用无DAG配置，businessName={}", JSON.toJSONString(businessInfoVO));
            return null;
        }

        Map<String, FunctionConfVO> functions = confData.getFunctions();
        Set<String> funcNames = confData.getBiz2func().get(businessName);
        List<FunctionConfVO> relatedConfs = funcNames.stream().map(x -> functions.get(x)).collect(Collectors.toList());

        long lastChangeTimestamp = -1;

        if (businessInfoVO.getUpdateTime().getTime() > lastChangeTimestamp) {
            lastChangeTimestamp = businessInfoVO.getUpdateTime().getTime();
        }

        if (executeDagConf.getUpdateTime().getTime() > lastChangeTimestamp) {
            lastChangeTimestamp = executeDagConf.getUpdateTime().getTime();
        }

        for (FunctionConf fConf : relatedConfs) {
            if (fConf.getUpdateTime().getTime() > lastChangeTimestamp) {
                lastChangeTimestamp = fConf.getUpdateTime().getTime();
            }
        }

        ConfigResponse.Item ret = new ConfigResponse.Item();
        ret.setLastChangeTimestamp(lastChangeTimestamp);
        BusinessConf businessConf = convert(businessInfoVO);
        businessConf.setChainRootId(executeDagConf.getRootId());
        ret.setBusinessConf(businessConf);
        ret.setNodeConfigs(convert(executeDagConf));
        return ret;
    }

    private Map<String, FunctionConfig> convert(Map<String, FunctionConfVO> functions) {
        Map<String, FunctionConfig> ret = Maps.newHashMap();
        for (Map.Entry<String, FunctionConfVO> entry : functions.entrySet()) {
            ret.put(entry.getKey(), convert(entry.getValue()));
        }
        return ret;
    }

    private FunctionConfig convert(FunctionConfVO confData) {
        FunctionConfig ret = new FunctionConfig();
        ret.setName(confData.getName());
        ret.setDesc(confData.getDesc());
        ret.setType(RiskCheckType.valueOf(confData.getType()));
        ret.setTimeoutMilliseconds(confData.getTimeoutMilliseconds().intValue());
        ret.setConf(confData.getConfObj());
        return ret;
    }

    private List<NodeConfig> convert(ExecuteDagConfVO executeDagConf) {
        return JSON.parseObject(executeDagConf.getConf(), new TypeReference<List<NodeConfig>>(){});
    }

    private BusinessConf convert(BusinessInfoWithBLOBs businessInfoWithBLOBs) {
        BusinessConf ret = new BusinessConf();
        ret.setName(businessInfoWithBLOBs.getName());
        ret.setDesc(businessInfoWithBLOBs.getDesc());
        if (StringUtils.isNotEmpty(businessInfoWithBLOBs.getType())) {
            try {
                ret.setType(BusinessType.valueOf(businessInfoWithBLOBs.getType()));
            } catch (Exception e) {
                logger.error("解析业务类型失败, conf={}", JSON.toJSONString(businessInfoWithBLOBs), e);
            }
        }
        ret.setAccessKey(businessInfoWithBLOBs.getName());
        ret.setSecretKey(businessInfoWithBLOBs.getSecretKey());
        ret.setQpsLimit(businessInfoWithBLOBs.getQpsLimit());
        ret.setRobotCheckConf(JSON.parseObject(businessInfoWithBLOBs.getRobotCheckConf(), CheckConf.class));
        ret.setUserCheckConf(JSON.parseObject(businessInfoWithBLOBs.getUserCheckConf(), CheckConf.class));
        ret.setAccessTargets(Sets.newHashSet(ACCESS_TARGEST));
        return ret;
    }

    @Override
    public FunctionConfVO functionOffline(Long id) {
        Preconditions.checkArgument(id != null, "id为null");
        FunctionConfVO function = functionConfigService.getFunction(id);
        Map<String, Set<String>> func2biz = onlineGroupedConf.get(function.getGroup()).getFunc2biz();
        Set<String> bizs = func2biz.get(function.getName());
        Preconditions.checkArgument(CollectionUtils.isEmpty(bizs), "function有业务在使用，" + JSON.toJSONString(bizs));
        return functionConfigService.functionOffline(id);
    }

    @Override
    public List<FunctionConfVO> syncFunction(String baseDir) throws Exception {
//        List<FunctionConfVO> ret = Lists.newArrayList();
        List<FunctionConfig> confs = FileUtils.loadContent(baseDir + "/functions.json", new com.alibaba.fastjson2.TypeReference<List<FunctionConfig>>() {
        });

        List<FunctionConfVO> vos = convert(confs);
        for (FunctionConfVO vo : vos) {
            FunctionConfVO record = syncOneFunc(vo);
//            ret.add(record);
        }

        refreshConf();
        return Lists.newArrayList(this.onlineGroupedConf.get(Finals.DEFAULT_GROUP).getFunctions().values());
    }

    private FunctionConfVO syncOneFunc(FunctionConfVO vo) throws Exception {
        FunctionConfVO ret;
        FunctionConfVO activeConf = functionConfigService.activeConf(vo.getGroup(), vo.getName());
        if (activeConf != null) {
            FunctionConfVO newVersion = functionConfigService.newFunctionVersion(activeConf);
            vo.setId(newVersion.getId());
            ret = functionConfigService.updateFunction(vo);
            ret = functionConfigService.upgradeFunction(ret);

        } else {
            ret = functionConfigService.newFunction(vo);
            ret  = functionConfigService.upgradeFunction(ret);
        }
        return ret;
    }

    private List<FunctionConfVO> convert(List<FunctionConfig> confs) {
        List<FunctionConfVO> ret = Lists.newArrayList();
        for (FunctionConfig conf : confs) {
            FunctionConfVO vo = convert(conf);
            ret.add(vo);
        }
        return ret;
    }

    private FunctionConfVO convert(FunctionConfig conf) {
        FunctionConfVO vo = new FunctionConfVO();
        vo.setName(conf.getName());
        vo.setGroup(Finals.DEFAULT_GROUP);
        vo.setDesc(StringUtils.isEmpty(conf.getDesc()) ? conf.getName() : conf.getDesc());
        vo.setType(conf.getType().name());
        vo.setConfObj(conf.getConf());
        vo.setTimeoutMilliseconds(conf.getTimeoutMilliseconds().longValue());
        return vo;
    }

    private ExecuteDagConfVO syncBuild(String baseDir, List<NodeConfig> nodeConf, String bizName, String chainRootId) {
        ExecuteDagConfVO ret = new ExecuteDagConfVO();
        ret.setConfArray(nodeConf);
        ret.setStatus(NormalStatus.edit.name());
        ret.setGroup(Finals.DEFAULT_GROUP);
        ret.setBusinessName(bizName);
        ret.setRootId(chainRootId);
        ret.setDesc("sync");

        fillExtraInfo(baseDir, ret.getConfArray());

        return ret;
    }

    private void fillExtraInfo(String baseDir, List<NodeConfig> confArray) {
        for (NodeConfig nConf : confArray) {
            RouterConfig routerConf = nConf.getRouterConf();
            switch (routerConf.getType()) {
                case groovy:
                    GroovyRouter.Conf conf = JSON.parseObject(JSON.toJSONString(routerConf.getConf()), GroovyRouter.Conf.class);

                    if (conf.getScript().endsWith(".groovy")) {
                        conf.setScriptRaw(conf.getScript());
                        try {
                            String script = FileUtils.loadContent(baseDir + "/groovy/" + conf.getScript());
                            conf.setScript(script);
                            routerConf.setConf(JSON.parseObject(JSON.toJSONString(conf)));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    break;
                case keyword:
                case user_end:
                case robot_end:
                case stupid_end:
                case simple_next:
                case risk_end:
                    break;
            }
        }
    }

    private BusinessInfoVO syncBuild(BusinessConf businessConf) {
        BusinessInfoVO ret = new BusinessInfoVO();
        ret.setName(businessConf.getName());
        ret.setGroup(Finals.DEFAULT_GROUP);
        ret.setDesc(businessConf.getDesc());
        ret.setType(businessConf.getType().name());
        ret.setSecretKey(businessConf.getSecretKey());
        ret.setQpsLimit(businessConf.getQpsLimit());
        ret.setStatus(NormalStatus.edit.name());

        ret.setUserCheckConfObj(businessConf.getUserCheckConf());
        ret.setRobotCheckConfObj(businessConf.getRobotCheckConf());

        return ret;
    }
}
