package com.jd.security.llmsec.contoller.rpc;

import com.google.common.collect.Lists;
import com.jd.security.llmsec.contoller.DefenseApiV2Controller;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponseX;
import com.jd.security.llmsec.core.rpc.DefenseApiService;
import com.jd.security.llmsec.service.MonitorAlarmService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;



//todo 使用长连接rpc方法对外提供服务
//@Service
public class DefenseApiServiceImpl implements DefenseApiService {
    private Logger logger = LoggerFactory.getLogger(DefenseApiServiceImpl.class);
//    @Autowired
    private DefenseApiV2Controller defenseApiV2Controller;

    @Override
    public ResponseMessage<List<DefenseApiResponseX>> invoke(String accessKey, DefenseApiRequest request) {
        MonitorAlarmService.requestIncr(accessKey, "rpc_all");
        long start = System.currentTimeMillis();
        logger.info("rpc invoke start: accessKey={}", accessKey);
        ResponseMessage<List<DefenseApiResponse>> responseMessage = (ResponseMessage<List<DefenseApiResponse>>)defenseApiV2Controller.defenseV2(accessKey, request);
        logger.info("rpc invoke end: accessKey={}, cost={}", accessKey, System.currentTimeMillis() - start);
        return convert(responseMessage);
    }

    private ResponseMessage<List<DefenseApiResponseX>> convert(ResponseMessage<List<DefenseApiResponse>> responseMessage) {
        ResponseMessage<List<DefenseApiResponseX>> ret = new ResponseMessage<>();
        ret.setCode(responseMessage.getCode());
        ret.setCost(responseMessage.getCost());
        ret.setMessage(responseMessage.getMessage());
        List<DefenseApiResponse> fromData = responseMessage.getData();
        if (fromData == null) {
            ret.setData(null);
        } else if (CollectionUtils.isEmpty(fromData)) {
            ret.setData(Lists.newArrayList());
        } else {
            ret.setData(convert(fromData));
        }

        return ret;
    }

    private List<DefenseApiResponseX> convert(List<DefenseApiResponse> fromData) {
        if (CollectionUtils.isEmpty(fromData)) {
            return Lists.newArrayList();
        }
        List<DefenseApiResponseX> ret = Lists.newArrayList();
        for (DefenseApiResponse response : fromData) {
            ret.add(convert(response));
        }
        return ret;
    }

    private DefenseApiResponseX convert(DefenseApiResponse response) {
        DefenseApiResponseX ret = new DefenseApiResponseX();
        ret.setRiskCode(response.getRiskCode());
        ret.setRiskMessage(response.getRiskMessage());
        ret.setRequests(response.getRequests());
        ret.setCheckedContent(response.getCheckedContent());
        ret.setRiskCheckType(response.getRiskCheckType().name());
        ret.setRiskCheckName(response.getRiskCheckName());
        ret.setRiskCheckResult(response.getRiskCheckResult());
        return ret;
    }
}
