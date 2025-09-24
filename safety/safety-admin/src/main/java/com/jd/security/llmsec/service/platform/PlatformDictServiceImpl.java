package com.jd.security.llmsec.service.platform;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.jd.security.llmsec.data.mapper.PlatformDictMapper;
import com.jd.security.llmsec.data.pojo.PlatformDict;
import com.jd.security.llmsec.data.pojo.PlatformDictExample;
import com.jd.security.llmsec.pojo.NormalStatus;
import com.jd.security.llmsec.pojo.platform.dict.DictVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



@Service
public class PlatformDictServiceImpl implements PlatformDictService {
    private Logger logger = LoggerFactory.getLogger(PlatformDictServiceImpl.class);

    @Autowired
    private PlatformDictMapper platformDictMapper;

    @Override
    public DictVo upgrade(DictVo req) {
        req.setId(null);

        Preconditions.checkArgument(StringUtils.isNotEmpty(req.getKey()), "key为空");
        Preconditions.checkArgument(req.getValueObj() != null, "valueObj为空");
        req.setValue(JSON.toJSONString(req.getValueObj()));

//        try {
//            JSON.parseObject(req.getValue());
//        } catch (Exception e) {
//            throw new RuntimeException("value不是合法的json" + e.getMessage());
//        }

        if (StringUtils.isEmpty(req.getDesc())) {
            req.setDesc("描述");
        }

        req.setStatus(NormalStatus.online.name());

        Preconditions.checkArgument(StringUtils.isNotEmpty(req.getReleaseErp()), "releaseErp为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(req.getReleaseName()), "releaseName为空");


        PlatformDictExample q = new PlatformDictExample();
        q.createCriteria().andKeyEqualTo(req.getKey());
        List<PlatformDict> platformDicts = platformDictMapper.selectByExample(q);
        if (CollectionUtils.isEmpty(platformDicts)) {
            req.setVersion(1);
            platformDictMapper.insertSelective(req);
        } else {
            for (PlatformDict t : platformDicts) {
                if (Objects.equals(NormalStatus.online.name(), t.getStatus())) {
                    changeStatus(t, NormalStatus.offline.name());
                }
            }
            List<Integer> versions = platformDicts.stream().map(PlatformDict::getVersion).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
            Integer maxVersion = versions.get(versions.size() - 1);
            req.setVersion(maxVersion + 1);
            platformDictMapper.insertSelective(req);
        }

        return req;
    }

    @Override
    public DictVo online(DictVo req) {
        String status = NormalStatus.online.name();
        return changeStatus(req, status);
    }


    private DictVo changeStatus(PlatformDict req, String status) {
        Preconditions.checkArgument(req.getId() != null, "id为空");
        PlatformDict record = new PlatformDict();
        record.setId(req.getId());
        record.setStatus(status);
        platformDictMapper.updateByPrimaryKeySelective(record);
        PlatformDict dict = platformDictMapper.selectByPrimaryKey(req.getId());
        return convert(dict);
    }

    private static @NotNull DictVo convert(PlatformDict dict) {
        DictVo ret = new DictVo();
        BeanUtils.copyProperties(dict, ret);
        if (ret.getValue().startsWith("{")) {
            ret.setValueObj(JSON.parseObject(ret.getValue()));
        } else if (ret.getValue().startsWith("[")) {
            ret.setValueObj(JSON.parseArray(ret.getValue()));
        }
        return ret;
    }

    @Override
    public DictVo offline(DictVo req) {
        String status = NormalStatus.offline.name();
        return changeStatus(req, status);
    }

    @Override
    public DictVo get(String key) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(key), "key为空");
        PlatformDictExample q = new PlatformDictExample();
        PlatformDictExample.Criteria criteria = q.createCriteria();
        criteria.andKeyEqualTo(key).andStatusEqualTo(NormalStatus.online.name());
        List<PlatformDict> platformDicts = platformDictMapper.selectByExampleWithBLOBs(q);
        if (CollectionUtils.isEmpty(platformDicts)) {
            return null;
        } else {
            return convert(platformDicts.get(0));
        }
    }

    @Override
    public List<DictVo> list(DictVo req) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(req.getKey()), "key为空");

        PlatformDictExample q = new PlatformDictExample();
        PlatformDictExample.Criteria criteria = q.createCriteria();
        if (StringUtils.isNotEmpty(req.getStatus())) {
            criteria.andStatusEqualTo(req.getStatus());
        }

        criteria.andKeyLike("%" + req.getKey() + "%");
        q.setOrderByClause(" update_time desc ");
        List<PlatformDict> platformDicts = platformDictMapper.selectByExampleWithBLOBs(q);
        List<DictVo> ret = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(platformDicts)) {
            platformDicts.forEach(x -> ret.add(convert(x)));
        }

        return ret;
    }
}
