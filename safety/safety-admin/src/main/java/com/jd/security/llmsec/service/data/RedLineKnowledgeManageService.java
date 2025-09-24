package com.jd.security.llmsec.service.data;

import com.jd.security.llmsec.data.pojo.RedLineKnowledge;
import com.jd.security.llmsec.data.pojo.RedLineKnowledgeWithBLOBs;
import com.jd.security.llmsec.pojo.data.RedLineKnowledgeVO;

import java.util.List;
import java.util.Map;



public interface RedLineKnowledgeManageService {
    RedLineKnowledgeWithBLOBs update(RedLineKnowledgeVO vo);
    RedLineKnowledgeWithBLOBs upsert(RedLineKnowledgeVO vo);

    RedLineKnowledgeWithBLOBs getById(Long id);

    RedLineKnowledgeWithBLOBs online(Long id);

    RedLineKnowledgeWithBLOBs offline(Long id);

    Map<String, RedLineKnowledgeWithBLOBs> getByWord(RedLineKnowledgeVO vo);
}
