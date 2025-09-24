package com.jd.security.llmsec.service.business;


import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.data.pojo.FunctionConfExample;
import com.jd.security.llmsec.pojo.business.FunctionConfVO;

import java.util.List;



public interface FunctionConfigService {
    FunctionConfVO newFunction(FunctionConfVO functionConf) throws Exception;

    void checkFunction(FunctionConfVO functionConf);

    void checkFunction(String confStr, RiskCheckType type);

    FunctionConfVO updateFunction(FunctionConfVO functionConf);

    FunctionConfVO functionOnline(Long id);

    FunctionConfVO functionOffline(Long id);

    FunctionConfVO activeConf(String group, String name);

    FunctionConfVO getFunction(Long id);

    List<FunctionConfVO> getFunction(FunctionConfExample query);

    FunctionConfVO newFunctionVersion(FunctionConfVO functionConf);

    FunctionConfVO upgradeFunction(FunctionConfVO newVersion);

    List<FunctionConfVO> allActiveConf();
}
