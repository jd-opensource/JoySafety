package com.jd.security.llmsec.service.business;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.conf.NodeConfig;
import com.jd.security.llmsec.core.conf.RouterConfig;
import com.jd.security.llmsec.core.engine.routers.GroovyRouter;
import com.jd.security.llmsec.core.engine.routers.SimpleNextRouter;
import com.jd.security.llmsec.data.mapper.BusinessInfoMapper;
import com.jd.security.llmsec.data.mapper.ExecuteDagConfMapper;
import com.jd.security.llmsec.data.pojo.ExecuteDagConf;
import com.jd.security.llmsec.data.pojo.ExecuteDagConfExample;
import com.jd.security.llmsec.pojo.business.BusinessInfoVO;
import com.jd.security.llmsec.pojo.business.ExecuteDagConfVO;
import com.jd.security.llmsec.pojo.business.FunctionConfVO;
import com.jd.security.llmsec.pojo.NormalStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;




@Service("DagConfigServiceImpl")
public class DagConfigServiceImpl implements DagConfigService {
    private Logger logger = LoggerFactory.getLogger(DagConfigServiceImpl.class);
    @Autowired
    private ExecuteDagConfMapper executeDagConfMapper;
    @Autowired
    private BusinessInfoMapper businessInfoMapper;
    @Autowired
    @Qualifier("BusinessConfigServiceImpl")
    private BusinessConfigService businessConfigService;
    @Autowired
    private FunctionConfigService functionConfigService;

    @Override
    public ExecuteDagConfVO newDag(ExecuteDagConfVO dagConf) {
        fullCheck(dagConf);

        cannotExistOnline(dagConf.getGroup(), dagConf.getBusinessName());
        dagConf.setStatus(NormalStatus.edit.name());
        dagConf.setVersion(newVersion(dagConf.getGroup(), dagConf.getBusinessName()));
        executeDagConfMapper.insertSelective(dagConf);
        ExecuteDagConf executeDagConf = executeDagConfMapper.selectByPrimaryKey(dagConf.getId());
        return unConvert(executeDagConf);
    }

    private void fullCheck(ExecuteDagConfVO dagConf) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(dagConf.getGroup()), "group为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(dagConf.getBusinessName()), "businessName为空");

        BusinessInfoVO businessInfoVO = businessConfigService.activeConf(dagConf.getGroup(), dagConf.getBusinessName());
        Preconditions.checkNotNull(businessInfoVO, "businessInfo为空");

        Preconditions.checkArgument(StringUtils.isNotEmpty(dagConf.getDesc()));
        Preconditions.checkArgument(StringUtils.isNotEmpty(dagConf.getRootId()));

        List<NodeConfig> confArray = dagConf.getConfArray();
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(confArray), "conf为空");

        checkConfArray(dagConf.getGroup(), dagConf.getRootId(), confArray);
        dagConf.setConf(JSON.toJSONString(dagConf.getConfArray(), SerializerFeature.SkipTransientField));
    }

    private ExecuteDagConfVO unConvert(ExecuteDagConf executeDagConf) {
        ExecuteDagConfVO ret = new ExecuteDagConfVO();
        BeanUtils.copyProperties(executeDagConf, ret);
        ret.setConfArray(JSON.parseArray(executeDagConf.getConf(), NodeConfig.class));
        return ret;
    }

    private void checkConfArray(String group, String rootId, List<NodeConfig> confArray) {
        Map<String, NodeConfig> nodeMap = Maps.newHashMap();
        for (NodeConfig conf : confArray) {
            Preconditions.checkArgument(StringUtils.isNotEmpty(conf.getNodeId()));
            nodeMap.put(conf.getNodeId(), conf);
        }
        Preconditions.checkArgument(nodeMap.containsKey(rootId), "rootId不存在");

        for (NodeConfig conf : confArray) {
            checkConf(group, conf, nodeMap);
        }
    }

    private void checkConf(String group, NodeConfig conf, Map<String, NodeConfig> nodeMap) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(conf.getNodeId()));

        Preconditions.checkArgument(conf.getFunctionConf() != null, "function为空");
        checkFunction(group, conf.getFunctionConf());

        Preconditions.checkArgument(conf.getRouterConf() != null, "router为空");
        checkRouter(conf.getRouterConf(), nodeMap);
    }

    private void checkRouter(RouterConfig routerConf, Map<String, NodeConfig> nodeMap) {
        Preconditions.checkArgument(routerConf.getType() != null, "type不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(routerConf.getName()), "name不能为空");
        switch (routerConf.getType()) {
            case groovy:
                checkGroovy(routerConf.getConf());
                break;
            case keyword:
            case user_end:
            case robot_end:
            case simple_next:
                checkSimpleNext(routerConf.getConf(), nodeMap);
                break;
            case risk_end:
                break;
            case stupid_end:
                break;
        }
    }

    private void checkSimpleNext(Map<String, Object> conf, Map<String, NodeConfig> nodeMap) {
        SimpleNextRouter.Conf routeConf = JSON.parseObject(JSON.toJSONString(conf), SimpleNextRouter.Conf.class);
        Preconditions.checkArgument(routeConf.getNodeId() != null, "nodeId为空");
        NodeConfig node = nodeMap.get(routeConf.getNodeId());
        Preconditions.checkArgument(node != null, "simplenext node不存在, nodeId=" + routeConf.getNodeId());
    }

    private void checkGroovy(Map<String, Object> conf) {
        GroovyRouter.Conf groovyConf = JSON.parseObject(JSON.toJSONString(conf), GroovyRouter.Conf.class);
        Preconditions.checkArgument(StringUtils.isNotEmpty(groovyConf.getScript()), "groovy router script不能为空");

    }

    private void checkFunction(String group, FunctionConfig functionConf) {
        String ref = functionConf.getRef();
        if (StringUtils.isNotEmpty(ref)) {
            FunctionConfVO functionConfVO = functionConfigService.activeConf(group, ref);
            Preconditions.checkArgument(functionConfVO != null, "function不存在，ref=" + ref);
        } else {
            // 不建议在dag中配置私有的function
            functionConfigService.checkFunction(JSON.toJSONString(functionConf.getConf()), functionConf.getType());
        }
    }

    private void cannotExistOnline(String group, String businessName) {
        ExecuteDagConfExample q = new ExecuteDagConfExample();
        q.createCriteria().andGroupEqualTo(group).andBusinessNameEqualTo(businessName).andStatusEqualTo(NormalStatus.online.name());
        long cnt = executeDagConfMapper.countByExample(q);
        Preconditions.checkArgument(cnt == 0, "dag已经存在了");
    }

    @Override
    public ExecuteDagConfVO getDag(Long id) {
        Preconditions.checkArgument(id != null && id > 0, "id != null && id > 0");
        ExecuteDagConf executeDagConf = executeDagConfMapper.selectByPrimaryKey(id);
        return executeDagConf != null ? unConvert(executeDagConf) : null;
    }

    @Override
    public ExecuteDagConfVO getDag(String group, String businessName, Integer dagVersion) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(group));
        Preconditions.checkArgument(StringUtils.isNotEmpty(businessName));
        Preconditions.checkArgument(dagVersion != null && dagVersion > 0);

        ExecuteDagConfExample q = new ExecuteDagConfExample();
        q.createCriteria().andBusinessNameEqualTo(businessName).andGroupEqualTo(group).andVersionEqualTo(dagVersion);
        List<ExecuteDagConf> executeDagConfs = executeDagConfMapper.selectByExampleWithBLOBs(q);
        return CollectionUtils.isNotEmpty(executeDagConfs) ? unConvert(executeDagConfs.get(0)) : null;
    }

    @Override
    public List<ExecuteDagConfVO> listAllVersion(String group, String businessName) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(group), "group为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(businessName), "businessName为空");

        ExecuteDagConfExample q = new ExecuteDagConfExample();
        q.createCriteria().andBusinessNameEqualTo(businessName).andGroupEqualTo(group);
        q.setOrderByClause(" version desc ");

        List<ExecuteDagConf> executeDagConfs = executeDagConfMapper.selectByExampleWithBLOBs(q);
        if (CollectionUtils.isEmpty(executeDagConfs)) {
            return Lists.newArrayList();
        } else {
            return executeDagConfs.stream().map(this::unConvert).collect(Collectors.toList());
        }
    }

    @Override
    public ExecuteDagConfVO newDagVersion(ExecuteDagConfVO dagConf) {
        Long id = dagConf.getId();
        ExecuteDagConf oldConf = null;
        Integer version = null;
        if (id != null) {
            oldConf = executeDagConfMapper.selectByPrimaryKey(id);
            Preconditions.checkArgument(oldConf != null, "id对应历史版本不存在");
            version = newVersion(oldConf.getGroup(), oldConf.getBusinessName());
        } else {
            if (StringUtils.isNotEmpty(dagConf.getGroup()) && StringUtils.isNotEmpty(dagConf.getBusinessName())) {
                List<ExecuteDagConf> allVersion = allVersion(dagConf.getGroup(), dagConf.getBusinessName());
                Preconditions.checkArgument(CollectionUtils.isNotEmpty(allVersion), "group和name对应历史版本不存在");
                version = allVersion.get(0).getVersion() + 1;
                oldConf = allVersion.get(0);
            } else {
                return null;
//                throw new RuntimeException("id != null or (group != null and businessName != null)");
            }
        }

        // Preconditions.checkArgument(Objects.equals(NormalStatus.online.name(), oldConf.getStatus()), "新版本需要基于线上版本");

        oldConf.setId(null);
        oldConf.setVersion(version);
        oldConf.setStatus(NormalStatus.edit.name());
        oldConf.setCreateTime(null);
        oldConf.setUpdateTime(new Date());

        executeDagConfMapper.insertSelective(oldConf);
        return unConvert(executeDagConfMapper.selectByPrimaryKey(oldConf.getId()));
    }

    private Integer newVersion(String group, String name) {
        List<ExecuteDagConf> executeDagConfs = allVersion(group, name);
        if (CollectionUtils.isEmpty(executeDagConfs)) {
            return 1;
        } else {
            return executeDagConfs.get(0).getVersion() + 1;
        }
    }

    private List<ExecuteDagConf> allVersion(String group, String name) {
        ExecuteDagConfExample q = new ExecuteDagConfExample();
        q.createCriteria().andGroupEqualTo(group).andBusinessNameEqualTo(name);
        q.setOrderByClause(" version desc");

        List<ExecuteDagConf> executeDagConfs = executeDagConfMapper.selectByExampleWithBLOBs(q);
        return executeDagConfs;
    }

    @Override
    public ExecuteDagConfVO updateDag(ExecuteDagConfVO dagConf) {
        fullCheck(dagConf);
        Preconditions.checkArgument(dagConf.getId() != null, "dagConf.getId() != null");
        Preconditions.checkArgument(!Objects.equals(NormalStatus.online.name(), dagConf.getStatus()), "不能更新online");
        dagConf.setStatus(NormalStatus.edit.name());
        dagConf.setUpdateTime(new Date());
        executeDagConfMapper.updateByPrimaryKeySelective(dagConf);
        return unConvert(executeDagConfMapper.selectByPrimaryKey(dagConf.getId()));
    }

    @Override
    public ExecuteDagConfVO dagOnline(Long id) {
        Preconditions.checkArgument(id != null, "id为null");
        ExecuteDagConf conf = new ExecuteDagConf();
        conf.setId(id);
        conf.setStatus(NormalStatus.online.name());
        conf.setUpdateTime(new Date());
        executeDagConfMapper.updateByPrimaryKeySelective(conf);
        return unConvert(executeDagConfMapper.selectByPrimaryKey(id));
    }

    @Override
    public ExecuteDagConfVO dagOffline(Long id) {
        Preconditions.checkArgument(id != null, "id为null");
        ExecuteDagConf conf = new ExecuteDagConf();
        conf.setId(id);
        conf.setStatus(NormalStatus.offline.name());
        conf.setUpdateTime(new Date());
        executeDagConfMapper.updateByPrimaryKeySelective(conf);
        return unConvert(executeDagConfMapper.selectByPrimaryKey(id));
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ExecuteDagConfVO upgrade(ExecuteDagConfVO newVersion) {
        Preconditions.checkArgument(newVersion.getId() !=null, "id不为null");
        ExecuteDagConf newConf = executeDagConfMapper.selectByPrimaryKey(newVersion.getId());
        Preconditions.checkArgument(newConf !=null, "newConf不为null");

        ExecuteDagConfVO q = new ExecuteDagConfVO();
        q.setGroup(newConf.getGroup());
        q.setBusinessName(newConf.getBusinessName());
        ExecuteDagConfVO activeConf = activeConf(q);
        if (activeConf == null) {
            dagOnline(newConf.getId());
        } else {
            offlineAndOnline(activeConf, newConf);
        }

        return unConvert(executeDagConfMapper.selectByPrimaryKey(newConf.getId()));
    }

    private void offlineAndOnline(ExecuteDagConfVO activeConf, ExecuteDagConf newConf) {
        activeConf.setStatus(NormalStatus.offline.name());
        newConf.setStatus(NormalStatus.online.name());
        activeConf.setUpdateTime(new Date());
        newConf.setUpdateTime(new Date());
        executeDagConfMapper.updateByPrimaryKeySelective(activeConf);
        executeDagConfMapper.updateByPrimaryKeySelective(newConf);
    }

    @Override
    public ExecuteDagConfVO activeConf(ExecuteDagConfVO dagConfVO) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(dagConfVO.getGroup()));
        Preconditions.checkArgument(StringUtils.isNotEmpty(dagConfVO.getBusinessName()));

        ExecuteDagConfExample q = new ExecuteDagConfExample();
        q.createCriteria().andBusinessNameEqualTo(dagConfVO.getBusinessName())
                .andGroupEqualTo(dagConfVO.getGroup())
                .andStatusEqualTo(NormalStatus.online.name());
        q.setOrderByClause(" update_time desc ");
        List<ExecuteDagConf> dagConfs = executeDagConfMapper.selectByExampleWithBLOBs(q);
        if (CollectionUtils.isNotEmpty(dagConfs)) {
            if (dagConfs.size() > 1) {
                logger.warn("online配置大于1，confs={}", JSON.toJSONString(dagConfs));
            }
            return unConvert(dagConfs.get(0));
        }
        return null;
    }

    @Override
    public List<ExecuteDagConfVO> allActiveConf() {
        ExecuteDagConfExample q = new ExecuteDagConfExample();
        q.createCriteria().andStatusEqualTo(NormalStatus.online.name());
        List<ExecuteDagConf> dagConfs = executeDagConfMapper.selectByExampleWithBLOBs(q);
        if (dagConfs == null) {
            return null;
        } else {
            return dagConfs.stream().map(x -> unConvert(x)).collect(Collectors.toList());
        }
    }
}
