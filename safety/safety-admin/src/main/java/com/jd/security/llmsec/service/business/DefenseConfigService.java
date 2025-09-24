package com.jd.security.llmsec.service.business;

import com.google.common.collect.Maps;
import com.jd.security.llmsec.contoller.business.DefenseConfigApiController;
import com.jd.security.llmsec.core.conf.ConfigReq;
import com.jd.security.llmsec.core.conf.ConfigResponse;
import com.jd.security.llmsec.pojo.business.BusinessInfoVO;
import com.jd.security.llmsec.pojo.business.ExecuteDagConfVO;
import com.jd.security.llmsec.pojo.business.FunctionConfVO;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;



public interface DefenseConfigService {

    List<BusinessInfoVO> func2Biz(DefenseConfigApiController.FuncBizReq req);

    List<FunctionConfVO> biz2Func(DefenseConfigApiController.FuncBizReq req);

    @Data
    class ConfData {
        private Map<String, BusinessInfoVO> business = Maps.newHashMap();
        private Map<String, ExecuteDagConfVO> dags = Maps.newHashMap();
        private Map<String, FunctionConfVO> functions = Maps.newHashMap();
        private Map<String, Set<String>> biz2func = Maps.newHashMap();
        private Map<String, Set<String>> func2biz = Maps.newHashMap();
    }

    ConfigResponse configs(ConfigReq req);

    FunctionConfVO functionOffline(Long id);

    List<FunctionConfVO> syncFunction(String baseDir) throws Exception;

}
