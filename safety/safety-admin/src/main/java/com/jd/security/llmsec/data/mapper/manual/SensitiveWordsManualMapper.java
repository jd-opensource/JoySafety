package com.jd.security.llmsec.data.mapper.manual;

import com.jd.security.llmsec.data.pojo.SensitiveWords;
import com.jd.security.llmsec.pojo.data.SensitiveWordsVO;

import java.util.List;



public interface SensitiveWordsManualMapper {
    List<SensitiveWords> selectByWhere(String whereClause);

    int upset(SensitiveWordsVO vo);

    int insertSelective(SensitiveWordsVO vo);
}
