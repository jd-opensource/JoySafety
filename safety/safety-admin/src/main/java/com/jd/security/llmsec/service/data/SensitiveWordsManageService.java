package com.jd.security.llmsec.service.data;

import com.jd.security.llmsec.data.pojo.SensitiveWords;
import com.jd.security.llmsec.pojo.data.SensitiveWordsVO;

import java.util.List;



public interface SensitiveWordsManageService {
    SensitiveWords updateWithId(SensitiveWordsVO vo);
    SensitiveWords upsert(SensitiveWordsVO vo);

    SensitiveWords getById(Long id);

    List<SensitiveWords> queryByWord(String word, SensitiveWordsVO vo);

    SensitiveWords online(Long id);

    SensitiveWords offline(Long id);

    //  切换版本到id对应版本
    SensitiveWords upgrade(Long id);
}
