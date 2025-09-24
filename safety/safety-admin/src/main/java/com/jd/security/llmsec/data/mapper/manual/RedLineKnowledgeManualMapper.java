package com.jd.security.llmsec.data.mapper.manual;

import com.jd.security.llmsec.data.pojo.RedLineKnowledgeWithBLOBs;
import com.jd.security.llmsec.data.pojo.SensitiveWords;
import com.jd.security.llmsec.pojo.data.RedLineKnowledgeVO;
import com.jd.security.llmsec.pojo.data.SensitiveWordsVO;

import java.util.List;



public interface RedLineKnowledgeManualMapper {
    List<RedLineKnowledgeWithBLOBs> selectByWhere(String whereClause);

    int upset(RedLineKnowledgeWithBLOBs vo);
}
