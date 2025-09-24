package com.jd.security.llmsec.service.business;


import com.jd.security.llmsec.pojo.business.ExecuteDagConfVO;

import java.util.List;



public interface DagConfigService {
    ExecuteDagConfVO newDag(ExecuteDagConfVO dagConf);

    ExecuteDagConfVO getDag(Long id);

    ExecuteDagConfVO getDag(String group, String businessName, Integer dagVersion);

    List<ExecuteDagConfVO> listAllVersion(String group, String businessName);

    ExecuteDagConfVO newDagVersion(ExecuteDagConfVO dagConf);

    ExecuteDagConfVO updateDag(ExecuteDagConfVO dagConf);

    ExecuteDagConfVO dagOnline(Long id);

    ExecuteDagConfVO dagOffline(Long id);

    ExecuteDagConfVO upgrade(ExecuteDagConfVO newVersion);

    ExecuteDagConfVO activeConf(ExecuteDagConfVO dagConfVO);

    List<ExecuteDagConfVO> allActiveConf();

}
