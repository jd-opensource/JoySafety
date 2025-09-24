package com.jd.security.llmsec.service.data;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.jd.security.llmsec.core.util.Md5Helper;
import com.jd.security.llmsec.data.mapper.RedLineKnowledgeMapper;
import com.jd.security.llmsec.data.mapper.manual.RedLineKnowledgeManualMapper;
import com.jd.security.llmsec.data.pojo.RedLineKnowledgeExample;
import com.jd.security.llmsec.data.pojo.RedLineKnowledgeWithBLOBs;
import com.jd.security.llmsec.pojo.NormalStatus;
import com.jd.security.llmsec.pojo.data.RedLineKnowledgeVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Service
public class RedLineKnowledgeManageServiceImpl implements RedLineKnowledgeManageService {
    private Logger logger = LoggerFactory.getLogger(RedLineKnowledgeManageServiceImpl.class);
    @Autowired
    private RedLineKnowledgeMapper redLineKnowledgeMapper;

    @Autowired
    private RedLineKnowledgeManualMapper redLineKnowledgeManualMapper;

    @Override
    public RedLineKnowledgeWithBLOBs update(RedLineKnowledgeVO vo) {
        Preconditions.checkNotNull(vo.getId());
        int effected = redLineKnowledgeMapper.updateByPrimaryKeySelective(vo);
        if (effected < 0) {
            throw new RuntimeException("id无对应数据");
        } else {
            return redLineKnowledgeMapper.selectByPrimaryKey(vo.getId());
        }
    }

    @Override
    public RedLineKnowledgeWithBLOBs upsert(RedLineKnowledgeVO vo) {
        Long id = vo.getId();
        check(vo);
        RedLineKnowledgeWithBLOBs ret;
        if (vo.getStatus() == null) {
            vo.setStatus(NormalStatus.online.name());
        }
        Date now = new Date();
        vo.setUpdateTime(now);
        if (id != null) {
            ret = update(vo);
        } else {
            vo.setVersion(1);
            vo.setCreateTime(now);
            ret = doUpset(vo);
        }
        return ret;
    }

    private void check(RedLineKnowledgeWithBLOBs vo) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getQuestion()));
        vo.setUniqId(Md5Helper.md5Hex(vo.getQuestion()));

        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getAnswer()));
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getBusinessScene()));
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getClassName()));
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getSource()));
    }

    private RedLineKnowledgeWithBLOBs doUpset(RedLineKnowledgeWithBLOBs vo) {
        redLineKnowledgeManualMapper.upset(vo);
        if (vo.getId() != null && vo.getId() > 0) {
            return redLineKnowledgeMapper.selectByPrimaryKey(vo.getId());
        } else {
            RedLineKnowledgeExample q = new RedLineKnowledgeExample();
            q.createCriteria().andUniqIdEqualTo(vo.getUniqId())
                    .andBusinessSceneEqualTo(vo.getBusinessScene())
                    .andVersionEqualTo(vo.getVersion());
            List<RedLineKnowledgeWithBLOBs> redLineKnowledgeWithBLOBs = redLineKnowledgeMapper.selectByExampleWithBLOBs(q);
            if (!redLineKnowledgeWithBLOBs.isEmpty()) {
                return redLineKnowledgeWithBLOBs.get(0);
            } else {
                return null;
            }
        }
    }

    @Override
    public RedLineKnowledgeWithBLOBs getById(Long id) {
        return redLineKnowledgeMapper.selectByPrimaryKey(id);
    }

    @Override
    public RedLineKnowledgeWithBLOBs online(Long id) {
        RedLineKnowledgeWithBLOBs sensitiveWords = new RedLineKnowledgeWithBLOBs();
        sensitiveWords.setId(id);
        sensitiveWords.setStatus(NormalStatus.online.name());
        int effected = redLineKnowledgeMapper.updateByPrimaryKeySelective(sensitiveWords);
        if (effected > 0) {
            return getById(id);
        } else {
            throw new RuntimeException("无对应的数据");
        }
    }

    @Override
    public RedLineKnowledgeWithBLOBs offline(Long id) {
        RedLineKnowledgeWithBLOBs sensitiveWords = new RedLineKnowledgeWithBLOBs();
        sensitiveWords.setId(id);
        sensitiveWords.setStatus(NormalStatus.offline.name());
        int effected = redLineKnowledgeMapper.updateByPrimaryKeySelective(sensitiveWords);
        if (effected > 0) {
            return getById(id);
        } else {
            throw new RuntimeException("无对应的数据");
        }
    }

    @Override
    public Map<String, RedLineKnowledgeWithBLOBs> getByWord(RedLineKnowledgeVO vo) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(vo.getQuestions()) || CollectionUtils.isNotEmpty(vo.getUniqIds()), "questions 和uniqIds不能同时为空");
        List<String> uniqIds = vo.getUniqIds();
        boolean hasQuestions = CollectionUtils.isNotEmpty(vo.getQuestions());
        if (hasQuestions) {
            uniqIds = vo.getQuestions().stream().map(Md5Helper::md5Hex).collect(Collectors.toList());
        }

        RedLineKnowledgeExample q = new RedLineKnowledgeExample();
        RedLineKnowledgeExample.Criteria criteria = q.createCriteria().andUniqIdIn(uniqIds).andStatusEqualTo(NormalStatus.online.name());
        if (StringUtils.isNotEmpty(vo.getBusinessScene())) {
            criteria.andBusinessSceneEqualTo(vo.getBusinessScene());
        }
        q.setOrderByClause(" id asc ");

        Map<String, RedLineKnowledgeWithBLOBs> ret = Maps.newHashMap();
        List<RedLineKnowledgeWithBLOBs> redLineKnowledgeWithBLOBs = redLineKnowledgeMapper.selectByExampleWithBLOBs(q);
        if (CollectionUtils.isNotEmpty(redLineKnowledgeWithBLOBs)) {
            redLineKnowledgeWithBLOBs.forEach(x -> {
                if (hasQuestions) {
                    ret.put(x.getQuestion(), x);
                } else {
                    ret.put(x.getUniqId(), x);
                }
            });
        }

        return ret;
    }
}
