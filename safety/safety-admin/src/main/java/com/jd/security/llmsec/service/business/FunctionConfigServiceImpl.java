package com.jd.security.llmsec.service.business;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.funcs.*;
import com.jd.security.llmsec.core.conf.funcs.SingleLabelPredConf.FasttextConf;
import com.jd.security.llmsec.data.mapper.FunctionConfMapper;
import com.jd.security.llmsec.data.pojo.FunctionConf;
import com.jd.security.llmsec.data.pojo.FunctionConfExample;
import com.jd.security.llmsec.pojo.business.FunctionConfVO;
import com.jd.security.llmsec.pojo.NormalStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



@Service
public class FunctionConfigServiceImpl implements FunctionConfigService {
    private Logger logger = LoggerFactory.getLogger(FunctionConfigServiceImpl.class);
    @Autowired
    private FunctionConfMapper functionConfMapper;

    @Override
    public FunctionConfVO newFunction(FunctionConfVO functionConf) throws Exception {
        checkFunction(functionConf);

        functionConf.setVersion(newVersion(functionConf.getGroup(), functionConf.getName()));
        functionConf.setStatus(NormalStatus.edit.name());

        boolean exist = functionExistOnline(functionConf.getGroup(), functionConf.getName());
        if (exist) {
            throw new Exception("function已经存在，可以更新已有的");
        }
        functionConf.setConf(JSON.toJSONString(functionConf.getConfObj()));
        functionConfMapper.insertSelective(functionConf);
        return functionConf;
    }

    @Override
    public void checkFunction(FunctionConfVO functionConf) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(functionConf.getName()), "name为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(functionConf.getGroup()), "group为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(functionConf.getDesc()), "desc为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(functionConf.getType()), "type为空");

        Long timeoutMilliseconds = functionConf.getTimeoutMilliseconds();
        Preconditions.checkArgument(timeoutMilliseconds != null && timeoutMilliseconds > 0, "timeoutMilliseconds不合法");

        RiskCheckType type;
        try {
            type = RiskCheckType.valueOf(functionConf.getType());
        } catch (Exception e) {
            logger.error("type不存在", e);
            throw e;
        }

        checkFunction(JSON.toJSONString(functionConf.getConfObj()), type);
    }

    @Override
    public void checkFunction(String confStr, RiskCheckType type) {
        switch (type) {
            case dummy:
                break;
            case single_label_pred:
                checkSingleLabel(confStr);
                break;
            case multi_label_pred:
                break;
            case keyword:
                checkKeyword(confStr);
                break;
            case kb_search:
            case rag_answer:
                checkKbSearch(confStr);
                break;
            case multi_turn_detect:
                checkMultiTurn(confStr);
                break;
            case parallel:
            case async:
            case mixed:
                break;
        }
    }

    private boolean functionExistOnline(String group, String name) {
        FunctionConfExample q = new FunctionConfExample();
        q.createCriteria().andGroupEqualTo(group).andNameEqualTo(name).andStatusEqualTo(NormalStatus.online.name());
        return functionConfMapper.countByExample(q) > 0;
    }


    private void checkMultiTurn(String confStr) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(confStr), "conf为空");
        MultiTurnDetectConf conf = JSON.parseObject(confStr, MultiTurnDetectConf.class);
        Preconditions.checkArgument(StringUtils.isNotEmpty(conf.getUrl()), "url为空");
        Preconditions.checkArgument(conf.getUrl().startsWith("http"), "url需要以http开头");
        Preconditions.checkArgument(conf.getMaxTurns() != null && conf.getMaxTurns() > 0, "maxTurns > 0");
    }

    private void checkKbSearch(String confStr) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(confStr), "conf为空");
        KbSearchConf conf = JSON.parseObject(confStr, KbSearchConf.class);
        Preconditions.checkArgument(StringUtils.isNotEmpty(conf.getUrl()), "url为空");
        Preconditions.checkArgument(conf.getTopK() != null && conf.getTopK() > 0, "topK > 0");
        Preconditions.checkArgument(conf.getThreshold() != null && conf.getThreshold() > 0.0, "threshold > 0.0");
    }

    private void checkKeyword(String confStr) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(confStr), "conf为空");
        KeywordConf conf = JSON.parseObject(confStr, KeywordConf.class);
        Preconditions.checkArgument(StringUtils.isNotEmpty(conf.getUrl()));
    }

    private void checkSingleLabel(String confStr) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(confStr), "conf为空");
        SingleLabelPredConf conf = JSON.parseObject(confStr, SingleLabelPredConf.class);
        Preconditions.checkArgument(conf.getModelType() != null, "conf.modelType is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(conf.getName()), "conf.name is null");
        switch (conf.getModelType()) {
            case bert:
            case textcnn:
            case llm:
            case fasttext:
                FasttextConf fasttextConf = JSON.parseObject(JSON.toJSONString(conf.getExtra()), FasttextConf.class);
                Preconditions.checkArgument(StringUtils.isNotEmpty(fasttextConf.getUrl()), "fasttext.url is null");
                break;
        }
    }

    @Override
    public FunctionConfVO updateFunction(FunctionConfVO functionConf) {
        Preconditions.checkArgument(functionConf.getId() != null, "id为null");
        cannotUpdateOnline(functionConf);
        checkFunction(functionConf);
        functionConf.setConf(JSON.toJSONString(functionConf.getConfObj()));
        functionConf.setStatus(NormalStatus.edit.name());
        functionConf.setUpdateTime(new Date());
        functionConfMapper.updateByPrimaryKeySelective(functionConf);
        return unConvert(functionConfMapper.selectByPrimaryKey(functionConf.getId()));
    }

    private FunctionConfVO unConvert(FunctionConf functionConf) {
        if (functionConf == null) {
            return null;
        }
        FunctionConfVO ret = new FunctionConfVO();
        BeanUtils.copyProperties(functionConf, ret);
        ret.setConfObj(JSON.parseObject(functionConf.getConf()));
        return ret;
    }

    private void cannotUpdateOnline(FunctionConfVO functionConf) {
        FunctionConf oldConf = functionConfMapper.selectByPrimaryKey(functionConf.getId());
        Preconditions.checkArgument(!Objects.equals(NormalStatus.online.name(), oldConf.getStatus()), "status=online不能更新");
    }

    @Override
    public FunctionConfVO functionOnline(Long id) {
        Preconditions.checkArgument(id != null, "id为null");
        onlineExist(id);
        FunctionConfVO conf = new FunctionConfVO();
        conf.setId(id);
        conf.setStatus(NormalStatus.online.name());
        conf.setUpdateTime(new Date());
        functionConfMapper.updateByPrimaryKeySelective(conf);
        return unConvert(functionConfMapper.selectByPrimaryKey(id));
    }

    private void onlineExist(Long id) {
        FunctionConf functionConf = functionConfMapper.selectByPrimaryKey(id);
        Preconditions.checkArgument(functionConf != null, "id对应配置不存在");
        FunctionConfVO activeConf = activeConf(functionConf.getGroup(), functionConf.getName());
        Preconditions.checkArgument(activeConf == null, "已有function在线上，需要先下线旧的");
    }

    @Override
    public FunctionConfVO functionOffline(Long id) {
        Preconditions.checkArgument(id != null, "id为null");
        FunctionConfVO conf = new FunctionConfVO();
        conf.setId(id);
        conf.setStatus(NormalStatus.offline.name());
        conf.setUpdateTime(new Date());
        functionConfMapper.updateByPrimaryKeySelective(conf);
        return unConvert(functionConfMapper.selectByPrimaryKey(id));
    }

    @Override
    public FunctionConfVO activeConf(String group, String name) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(group), "group不为null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(name), "name不为null");

        FunctionConfExample q = new FunctionConfExample();
        q.createCriteria().andGroupEqualTo(group).andNameEqualTo(name).andStatusEqualTo(NormalStatus.online.name());
        q.setOrderByClause(" update_time desc limit 1");
        List<FunctionConf> functionConfs = functionConfMapper.selectByExampleWithBLOBs(q);
        return CollectionUtils.isNotEmpty(functionConfs) ? unConvert(functionConfs.get(0)) : null;
    }

    @Override
    public FunctionConfVO getFunction(Long id) {
        Preconditions.checkArgument(id !=null, "id不为null");
        return unConvert(functionConfMapper.selectByPrimaryKey(id));
    }

    @Override
    public List<FunctionConfVO> getFunction(FunctionConfExample query) {
        Preconditions.checkNotNull(query);
        List<FunctionConf> functionConfs = functionConfMapper.selectByExampleWithBLOBs(query);
        return CollectionUtils.isNotEmpty(functionConfs) ? functionConfs.stream().map(x -> unConvert(x)).collect(Collectors.toList()) : null;
    }

    @Override
    public FunctionConfVO newFunctionVersion(FunctionConfVO functionConf) {
        Preconditions.checkArgument(functionConf.getId() !=null, "id不为null");
        FunctionConf oldConf = functionConfMapper.selectByPrimaryKey(functionConf.getId());

        Preconditions.checkArgument(Objects.equals(NormalStatus.online.name(), oldConf.getStatus()), "只能从当前线上版本创建新版本");

        oldConf.setId(null);
        oldConf.setVersion(newVersion(oldConf.getGroup(), oldConf.getName()));
        oldConf.setStatus(NormalStatus.edit.name());
        oldConf.setCreateTime(null);
        oldConf.setUpdateTime(new Date());

        functionConfMapper.insertSelective(oldConf);
        return unConvert(functionConfMapper.selectByPrimaryKey(oldConf.getId()));
    }

    private Integer newVersion(String group, String name) {
        FunctionConfExample q = new FunctionConfExample();
        q.createCriteria().andGroupEqualTo(group).andNameEqualTo(name);
        q.setOrderByClause(" version desc limit 1");

        List<FunctionConf> functionConfs = functionConfMapper.selectByExampleWithBLOBs(q);
        if (CollectionUtils.isEmpty(functionConfs)) {
            return 1;
        } else {
            return functionConfs.get(0).getVersion() + 1;
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public FunctionConfVO upgradeFunction(FunctionConfVO newVersion) {
        Preconditions.checkArgument(newVersion.getId() !=null, "id不为null");
        FunctionConf newConf = functionConfMapper.selectByPrimaryKey(newVersion.getId());
        Preconditions.checkArgument(newConf !=null, "newConf不为null");

        checkFunction(unConvert(newConf));
        FunctionConfVO oldConf = activeConf(newConf.getGroup(), newConf.getName());
        if (oldConf == null) {
            functionOnline(newVersion.getId());
        } else {
            offlineAndOnline(oldConf, unConvert(newConf));
        }

        return unConvert(functionConfMapper.selectByPrimaryKey(newVersion.getId()));
    }

    public void offlineAndOnline(FunctionConfVO oldConf, FunctionConfVO newConf) {
        oldConf.setStatus(NormalStatus.offline.name());
        newConf.setStatus(NormalStatus.online.name());
        oldConf.setUpdateTime(new Date());
        newConf.setUpdateTime(new Date());
        functionConfMapper.updateByPrimaryKeySelective(oldConf);
        functionConfMapper.updateByPrimaryKeySelective(newConf);
    }

    @Override
    public List<FunctionConfVO> allActiveConf() {
        FunctionConfExample q = new FunctionConfExample();
        q.createCriteria().andStatusEqualTo(NormalStatus.online.name());

        List<FunctionConf> functionConfs = functionConfMapper.selectByExampleWithBLOBs(q);
        if (functionConfs == null) {
            return Lists.newArrayList();
        } else {
            return functionConfs.stream().map(x -> unConvert(x)).collect(Collectors.toList());
        }
    }
}
