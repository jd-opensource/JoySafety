package com.jd.security.llmsec.service.business;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.jd.security.llmsec.core.session.BusinessType;
import com.jd.security.llmsec.core.session.CheckConf;
import com.jd.security.llmsec.data.mapper.BusinessInfoMapper;
import com.jd.security.llmsec.data.pojo.BusinessInfoExample;
import com.jd.security.llmsec.data.pojo.BusinessInfoWithBLOBs;
import com.jd.security.llmsec.pojo.business.BusinessInfoVO;
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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



@Service("BusinessConfigServiceImpl")
public class BusinessConfigServiceImpl implements BusinessConfigService {
    private Logger logger = LoggerFactory.getLogger(BusinessConfigServiceImpl.class);
    @Autowired
    private BusinessInfoMapper businessInfoMapper;

    @Override
    public BusinessInfoVO newBusiness(BusinessInfoVO businessConf) {
        voCheck(businessConf);
        cannotExistOnline(businessConf.getGroup(), businessConf.getName());
        businessConf.setAccessTargets(JSON.toJSONString(businessConf.getAccessTargetsArray()));
        businessConf.setRobotCheckConf(JSON.toJSONString(businessConf.getRobotCheckConfObj()));
        businessConf.setUserCheckConf(JSON.toJSONString(businessConf.getUserCheckConfObj()));

        BusinessInfoWithBLOBs newConf = new BusinessInfoVO();
        BeanUtils.copyProperties(businessConf, newConf);

        newConf.setStatus(NormalStatus.edit.name());
        newConf.setVersion(newVersion(businessConf.getGroup(), businessConf.getName()));
        newConf.setUpdateTime(new Date());
        businessInfoMapper.insertSelective(newConf);

        BusinessInfoWithBLOBs businessInfoWithBLOBs = businessInfoMapper.selectByPrimaryKey(newConf.getId());
        return unConvert(businessInfoWithBLOBs);
    }

    private void voCheck(BusinessInfoVO businessConf) {
        recordCheck(businessConf);

        checkCheckConf(businessConf.getRobotCheckConfObj());
        checkCheckConf(businessConf.getUserCheckConfObj());
    }

    private void recordCheck(BusinessInfoWithBLOBs businessConf) {
        Preconditions.checkNotNull(businessConf, "conf为null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(businessConf.getGroup()), "group为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(businessConf.getName()), "name为空");

        Preconditions.checkArgument(StringUtils.isNotEmpty(businessConf.getType()), "type为空");
        try {
            BusinessType.valueOf(businessConf.getType());
        } catch (Exception e) {
            logger.error("type不存在，type={}", businessConf.getType(), e);
            throw new RuntimeException(e);
        }

//        Preconditions.checkArgument(StringUtils.isNotEmpty(businessConf.getAccessKey()), "group为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(businessConf.getSecretKey()), "secretKey为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(businessConf.getDesc()), "info为空");
        Integer qpsLimit = businessConf.getQpsLimit();
        Preconditions.checkArgument(qpsLimit != null && qpsLimit > 0, "qpsLimit != null && qpsLimit > 0");
    }

    private BusinessInfoVO unConvert(BusinessInfoWithBLOBs businessInfoWithBLOBs) {
        if (businessInfoWithBLOBs == null) {
            return null;
        }
        BusinessInfoVO ret = new BusinessInfoVO();
        BeanUtils.copyProperties(businessInfoWithBLOBs, ret);
        if (StringUtils.isNotEmpty(businessInfoWithBLOBs.getAccessTargets())) {
            ret.setAccessTargetsArray(Sets.newHashSet(JSON.parseArray(businessInfoWithBLOBs.getAccessTargets(), String.class)));
        }
        if (StringUtils.isNotEmpty(businessInfoWithBLOBs.getRobotCheckConf())) {
            ret.setRobotCheckConfObj(JSON.parseObject(businessInfoWithBLOBs.getRobotCheckConf(), CheckConf.class));
        }
        if (StringUtils.isNotEmpty(businessInfoWithBLOBs.getUserCheckConf())) {
            ret.setUserCheckConfObj(JSON.parseObject(businessInfoWithBLOBs.getUserCheckConf(), CheckConf.class));
        }
        return ret;
    }

    private void checkCheckConf(CheckConf checkConf) {
        Preconditions.checkNotNull(checkConf, "checkConf为空");
        Preconditions.checkNotNull(checkConf.getUnit(), "checkConf.unit为空");
        int checkNum = checkConf.getCheckNum();
        Long timeoutMilliseconds = checkConf.getTimeoutMilliseconds();
        Preconditions.checkArgument(checkNum > 0, "checkConf.checkNum > 0");
        Preconditions.checkArgument(timeoutMilliseconds > 0, "checkConf.timeoutMilliseconds > 0");
    }

    private void cannotExistOnline(String group, String name) {
        BusinessInfoExample q = new BusinessInfoExample();
        q.createCriteria().andGroupEqualTo(group).andNameEqualTo(name).andStatusEqualTo(NormalStatus.online.name());
        Preconditions.checkArgument(businessInfoMapper.countByExample(q) == 0, "group-name已经上线了");
    }

    @Override
    public BusinessInfoVO getBusiness(Long id) {
        Preconditions.checkNotNull(id, "id!=null");
        BusinessInfoWithBLOBs businessInfoWithBLOBs = businessInfoMapper.selectByPrimaryKey(id);
        return unConvert(businessInfoWithBLOBs);
    }

    @Override
    public List<BusinessInfoVO> listAllVersions(BusinessInfoVO req) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(req.getGroup()), "group != null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(req.getName()), "name != null");

        BusinessInfoExample q = new BusinessInfoExample();
        q.createCriteria().andGroupEqualTo(req.getGroup()).andNameEqualTo(req.getName());
        q.setOrderByClause(" version desc");
        List<BusinessInfoWithBLOBs> businessInfoWithBLOBs = businessInfoMapper.selectByExampleWithBLOBs(q);
        return businessInfoWithBLOBs.stream().map(x -> unConvert(x)).collect(Collectors.toList());
    }

    @Override
    public BusinessInfoVO newBusinessVersion(BusinessInfoVO businessConf) {
        // Preconditions.checkNotNull(businessConf.getId(), "id != null");
        BusinessInfoWithBLOBs oldConf = null;
        if (businessConf.getId() != null) {
            oldConf = businessInfoMapper.selectByPrimaryKey(businessConf.getId());
            Preconditions.checkNotNull(oldConf, "id对应版本不存在");
        } else {
            if (StringUtils.isEmpty(businessConf.getGroup()) || StringUtils.isEmpty(businessConf.getName())) {
                throw new RuntimeException("id != null or (group != null and name != null)");
            }
            BusinessInfoExample q = new BusinessInfoExample();
            q.createCriteria().andGroupEqualTo(businessConf.getGroup()).andNameEqualTo(businessConf.getName());
            q.setOrderByClause(" version desc");
            List<BusinessInfoWithBLOBs> businessInfoWithBLOBs = businessInfoMapper.selectByExampleWithBLOBs(q);
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(businessInfoWithBLOBs), "无历史版本");
            oldConf = businessInfoWithBLOBs.get(0);
        }

        Preconditions.checkArgument(oldConf != null, "不存在历史版本");

        oldConf.setId(null);
        oldConf.setVersion(newVersion(oldConf.getGroup(), oldConf.getName()));
        oldConf.setStatus(NormalStatus.edit.name());
        oldConf.setCreateTime(null);
        oldConf.setUpdateTime(null);

        businessInfoMapper.insertSelective(oldConf);
        return unConvert(businessInfoMapper.selectByPrimaryKey(oldConf.getId()));
    }

    private Integer newVersion(String group, String name) {
        BusinessInfoExample q = new BusinessInfoExample();
        q.createCriteria().andGroupEqualTo(group).andNameEqualTo(name);
        q.setOrderByClause(" version desc limit 1");

        List<BusinessInfoWithBLOBs> businessInfoWithBLOBs = businessInfoMapper.selectByExampleWithBLOBs(q);
        if (CollectionUtils.isEmpty(businessInfoWithBLOBs)) {
            return 1;
        } else {
            return businessInfoWithBLOBs.get(0).getVersion() + 1;
        }
    }

    @Override
    public BusinessInfoVO updateBusiness(BusinessInfoVO businessConf) {
        Preconditions.checkNotNull(businessConf.getId(), "id != null");
        Preconditions.checkArgument(!Objects.equals(NormalStatus.online.name(), businessConf.getStatus()), "不能更新online");
        voCheck(businessConf);
        businessConf.setStatus(NormalStatus.edit.name());
        businessConf.setUserCheckConf(JSON.toJSONString(businessConf.getUserCheckConfObj()));
        businessConf.setRobotCheckConf(JSON.toJSONString(businessConf.getRobotCheckConfObj()));
        businessConf.setUpdateTime(new Date());
        businessInfoMapper.updateByPrimaryKeyWithBLOBs(businessConf);
        return unConvert(businessInfoMapper.selectByPrimaryKey(businessConf.getId()));
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BusinessInfoVO upgradeBusiness(BusinessInfoVO newVersion) {
        Preconditions.checkArgument(newVersion.getId() != null, "id为空");
        BusinessInfoWithBLOBs newConf = businessInfoMapper.selectByPrimaryKey(newVersion.getId());
        Preconditions.checkArgument(newConf !=null, "newConf不为null");
        voCheck(unConvert(newConf));
        BusinessInfoVO oldConf = activeConf(newConf.getGroup(), newConf.getName());

        if (oldConf == null) {
            businessOnline(newVersion.getId());
        } else {
            offlineAndOnline(oldConf, unConvert(newConf));
        }

        return unConvert(businessInfoMapper.selectByPrimaryKey(newVersion.getId()));
    }

    private void offlineAndOnline(BusinessInfoVO oldConf, BusinessInfoVO newConf) {
        oldConf.setStatus(NormalStatus.offline.name());
        newConf.setStatus(NormalStatus.online.name());
        oldConf.setUpdateTime(new Date());
        newConf.setUpdateTime(new Date());
        businessInfoMapper.updateByPrimaryKeySelective(oldConf);
        businessInfoMapper.updateByPrimaryKeySelective(newConf);
    }

    @Override
    public BusinessInfoVO businessOnline(Long id) {
        Preconditions.checkNotNull(id, "id != null");
        BusinessInfoWithBLOBs conf = businessInfoMapper.selectByPrimaryKey(id);
        recordCheck(conf);

        conf.setStatus(NormalStatus.online.name());
        conf.setUpdateTime(new Date());
        businessInfoMapper.updateByPrimaryKeySelective(conf);
        return unConvert(businessInfoMapper.selectByPrimaryKey(id));
    }

    @Override
    public BusinessInfoVO businessOffline(Long id) {
        Preconditions.checkNotNull(id, "id != null");
        BusinessInfoWithBLOBs conf = businessInfoMapper.selectByPrimaryKey(id);
        conf.setStatus(NormalStatus.offline.name());
        conf.setUpdateTime(new Date());
        businessInfoMapper.updateByPrimaryKeySelective(conf);
        return unConvert(businessInfoMapper.selectByPrimaryKey(id));
    }

    @Override
    public BusinessInfoVO activeConf(String group, String businessName) {
        return activeBusinessInfoData(group, businessName);
    }

    private BusinessInfoVO activeBusinessInfoData(String group, String businessName) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(group), "group为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(businessName), "businessName为空");
        BusinessInfoExample q = new BusinessInfoExample();
        q.createCriteria().andNameEqualTo(businessName)
                .andGroupEqualTo(group)
                .andStatusEqualTo(NormalStatus.online.name());
        q.setOrderByClause(" update_time desc limit 1");
        List<BusinessInfoWithBLOBs> businessInfoWithBLOBs = businessInfoMapper.selectByExampleWithBLOBs(q);
        return CollectionUtils.isNotEmpty(businessInfoWithBLOBs) ? unConvert(businessInfoWithBLOBs.get(0)) : null;
    }

    @Override
    public List<BusinessInfoVO> allActiveConf() {
        BusinessInfoExample q = new BusinessInfoExample();
        q.createCriteria().andStatusEqualTo(NormalStatus.online.name());
        List<BusinessInfoWithBLOBs> businessInfoWithBLOBs = businessInfoMapper.selectByExampleWithBLOBs(q);
        if (businessInfoWithBLOBs == null) {
            return null;
        }
        return businessInfoWithBLOBs.stream().map(x -> unConvert(x)).collect(Collectors.toList());
    }
}
