package com.jd.security.llmsec.service.data;

import com.google.common.base.Preconditions;
import com.jd.security.llmsec.core.util.Md5Helper;
import com.jd.security.llmsec.data.mapper.SensitiveWordsMapper;
import com.jd.security.llmsec.data.mapper.manual.SensitiveWordsManualMapper;
import com.jd.security.llmsec.data.pojo.SensitiveWords;
import com.jd.security.llmsec.data.pojo.SensitiveWordsExample;
import com.jd.security.llmsec.pojo.NormalStatus;
import com.jd.security.llmsec.pojo.data.SensitiveWordsVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;




@Service
public class SensitiveWordsManageServiceImpl implements SensitiveWordsManageService {
    private Logger logger = LoggerFactory.getLogger(SensitiveWordsManageServiceImpl.class);
    @Autowired
    private SensitiveWordsMapper sensitiveWordsMapper;
    @Autowired
    private SensitiveWordsManualMapper sensitiveWordsManualMapper;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SensitiveWords updateWithId(SensitiveWordsVO vo) {
        /*
        0. 获取原有所有字段值
        1. merge所有字段值到最新版本，并创建新版本
        2. 上线版本切换为新版本
         */
        Preconditions.checkNotNull(vo.getId(), "id can not be null");

        SensitiveWords sensitiveWordsInDb = sensitiveWordsMapper.selectByPrimaryKey(vo.getId());
        if (sensitiveWordsInDb == null) {
            throw new RuntimeException("id无对应数据");
        }

        sensitiveWordsInDb = mergeFields(sensitiveWordsInDb, vo);
        check(sensitiveWordsInDb);

        Date now = new Date();
        sensitiveWordsInDb.setId(null);
        sensitiveWordsInDb.setCreateTime(now);
        sensitiveWordsInDb.setUpdateTime(now);
        Integer nextVersion = nextVersion(vo.getWord(), vo.getBusinessScene());
        sensitiveWordsInDb.setVersion(nextVersion);
        sensitiveWordsInDb.setStatus(NormalStatus.offline.name()); // 新版本默认下线
        sensitiveWordsMapper.insertSelective(sensitiveWordsInDb);
        return upgrade(sensitiveWordsInDb.getId());
    }

    private Integer nextVersion(String word, String businessScene) {
        String uniqId = Md5Helper.md5Hex(word);
        SensitiveWordsExample q = new SensitiveWordsExample();
        q.createCriteria().andUniqIdEqualTo(uniqId).andBusinessSceneEqualTo(businessScene);
        q.setOrderByClause(" version desc limit 1 ");
        List<SensitiveWords> sensitiveWords = sensitiveWordsMapper.selectByExample(q);
        if (CollectionUtils.isEmpty(sensitiveWords)) {
            return 1;
        } else {
            return sensitiveWords.get(0).getVersion() + 1;
        }
    }

    private SensitiveWords mergeFields(SensitiveWords sensitiveWordsInDb, SensitiveWordsVO vo) {
        if (StringUtils.isNotEmpty(vo.getBusinessScene())) {
            sensitiveWordsInDb.setBusinessScene(vo.getBusinessScene());
        }

        if (StringUtils.isNotEmpty(vo.getWord())) {
            sensitiveWordsInDb.setWord(vo.getWord());
        }

        if (StringUtils.isNotEmpty(vo.getTags())) {
            sensitiveWordsInDb.setTags(vo.getTags());
        }

        if (StringUtils.isNotEmpty(vo.getMatchType())) {
            sensitiveWordsInDb.setMatchType(vo.getMatchType());
        }

        if (StringUtils.isNotEmpty(vo.getHandleStrategy())) {
            sensitiveWordsInDb.setHandleStrategy(vo.getHandleStrategy());
        }

        if (StringUtils.isNotEmpty(vo.getSource())) {
            sensitiveWordsInDb.setSource(vo.getSource());
        }

        if (StringUtils.isNotEmpty(vo.getDesc())) {
            sensitiveWordsInDb.setDesc(vo.getDesc());
        }

        if (StringUtils.isNotEmpty(vo.getStatus())) {
            sensitiveWordsInDb.setStatus(vo.getStatus());
        }

        if (StringUtils.isNotEmpty(vo.getEditorErp())) {
            sensitiveWordsInDb.setEditorErp(vo.getEditorErp());
        }

        if (StringUtils.isNotEmpty(vo.getEditorName())) {
            sensitiveWordsInDb.setEditorName(vo.getEditorName());
        }

        if (StringUtils.isNotEmpty(vo.getFirstClassName())) {
            sensitiveWordsInDb.setFirstClassName(vo.getFirstClassName());
        }

        if (vo.getFirstClassNo() != null) {
            sensitiveWordsInDb.setFirstClassNo(vo.getFirstClassNo());
        }

        if (StringUtils.isNotEmpty(vo.getSecondClassName())) {
            sensitiveWordsInDb.setSecondClassName(vo.getSecondClassName());
        }

        if (vo.getSecondClassNo() != null) {
            sensitiveWordsInDb.setSecondClassNo(vo.getSecondClassNo());
        }

        return sensitiveWordsInDb;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SensitiveWords upsert(SensitiveWordsVO vo) {
        Long id = vo.getId();

        if (id != null) { // id存在时直接版本更新
            return updateWithId(vo);
        } else { // id不存在时，查询当前在线版本
            Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getWord()), "word为空");
            Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getBusinessScene()), "businessScene为空");
            SensitiveWords active = active(vo.getBusinessScene(), Md5Helper.md5Hex(vo.getWord()));
            if (active != null) {
                vo.setId(active.getId());
                return updateWithId(vo);
            }

            check(vo);
            Date now = new Date();
            vo.setVersion(1);
            vo.setCreateTime(now);
            vo.setUpdateTime(now);
            vo.setStatus(NormalStatus.online.name());
            sensitiveWordsMapper.insertSelective(vo);
            return sensitiveWordsMapper.selectByPrimaryKey(vo.getId());
        }
    }

    private void check(SensitiveWords vo) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getWord()), "word为空");
        vo.setUniqId(Md5Helper.md5Hex(vo.getWord()));

        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getBusinessScene()), "businessScene为空");

        Preconditions.checkArgument(vo.getFirstClassNo() != null && vo.getFirstClassNo() >= 0, "firstClassNo不合法");
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getFirstClassName()), "firstClassName为空");
        Preconditions.checkArgument(vo.getSecondClassNo() != null && vo.getSecondClassNo() >= 0, "secondClassNo不合法");
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getSecondClassName()), "secondClassName为空");

        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getMatchType()), "matchType为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getHandleStrategy()), "handleStragegy为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(vo.getSource()), "source为空");
    }


    @Override
    public SensitiveWords getById(Long id) {
        return sensitiveWordsMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<SensitiveWords> queryByWord(String word, SensitiveWordsVO vo) {
        SensitiveWordsExample q = new SensitiveWordsExample();
        SensitiveWordsExample.Criteria criteria = q.createCriteria().andWordLike("%" + word + "%");
        if (StringUtils.isNotEmpty(vo.getStatus())) {
            criteria.andStatusEqualTo(vo.getStatus());
        }

        if (StringUtils.isNotEmpty(vo.getBusinessScene())) {
            criteria.andBusinessSceneLike("%" + vo.getBusinessScene() + "%");
        }

        q.setOrderByClause(" update_time desc limit 100 ");
        return sensitiveWordsMapper.selectByExampleWithBLOBs(q);
    }

    @Override
    public SensitiveWords online(Long id) {
        SensitiveWords sensitiveWords = new SensitiveWords();
        sensitiveWords.setId(id);
        sensitiveWords.setStatus(NormalStatus.online.name());
        int effected = sensitiveWordsMapper.updateByPrimaryKeySelective(sensitiveWords);
        if (effected > 0) {
            return getById(id);
        } else {
            throw new RuntimeException("无对应的数据");
        }
    }

    @Override
    public SensitiveWords offline(Long id) {
        SensitiveWords sensitiveWords = new SensitiveWords();
        sensitiveWords.setId(id);
        sensitiveWords.setStatus(NormalStatus.offline.name());
        int effected = sensitiveWordsMapper.updateByPrimaryKeySelective(sensitiveWords);
        if (effected > 0) {
            return getById(id);
        } else {
            throw new RuntimeException("无对应的数据");
        }
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public SensitiveWords upgrade(Long id) {
        SensitiveWords sensitiveWords = sensitiveWordsMapper.selectByPrimaryKey(id);
        if (sensitiveWords == null) {
            throw new RuntimeException("id对应敏感词不存在");
        }

        // 无须上线
        if (NormalStatus.online.name().equals(sensitiveWords.getStatus())) {
            return sensitiveWords;
        }

        // 下线旧的
        SensitiveWords activeWord = active(sensitiveWords.getBusinessScene(), sensitiveWords.getUniqId());
        if (activeWord != null) {
            activeWord.setStatus(NormalStatus.offline.name());
            activeWord.setUpdateTime(new Date());
            sensitiveWordsMapper.updateByPrimaryKeySelective(activeWord);
        }

        // 上线新的
        sensitiveWords.setStatus(NormalStatus.online.name());
        sensitiveWords.setUpdateTime(new Date());
        sensitiveWordsMapper.updateByPrimaryKeySelective(sensitiveWords);
        return sensitiveWords;
    }

    private SensitiveWords active(String businessScene, String uniqId) {
        SensitiveWordsExample q = new SensitiveWordsExample();
        q.createCriteria().andUniqIdEqualTo(uniqId).andBusinessSceneEqualTo(businessScene)
                .andStatusEqualTo(NormalStatus.online.name());
        q.setOrderByClause(" update_time desc limit 1 ");
        List<SensitiveWords> sensitiveWords = sensitiveWordsMapper.selectByExampleWithBLOBs(q);
        if (sensitiveWords == null || sensitiveWords.isEmpty()) {
            return null;
        } else {
            return sensitiveWords.get(0);
        }
    }
}
