package com.jd.security.llmsec.service.platform;

import com.jd.security.llmsec.pojo.platform.dict.DictVo;

import java.util.List;



public interface PlatformDictService {
    DictVo upgrade(DictVo req);

    DictVo online(DictVo req);

    DictVo offline(DictVo req);

    DictVo get(String key);

    List<DictVo> list(DictVo req);
}
