package com.jd.security.llmsec.contoller;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.jd.security.llmsec.core.ResponseCode;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.api.defense.DefenseResultFetchRequest;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.ResponseMode;
import com.jd.security.llmsec.pojo.ConstantVals;
import com.jd.security.llmsec.service.DefenseService;
import com.jd.security.llmsec.service.MonitorAlarmService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



@RestController
@RequestMapping("/llmsec/api")
public class DefenseApiV2Controller {
    private Logger logger = LoggerFactory.getLogger(DefenseApiV2Controller.class);
    @Autowired
    private DefenseService defenseService;
    @PostMapping("/defense/v2/{accessKey}")
    public Object defenseV2(@PathVariable String accessKey,
                            @RequestBody DefenseApiRequest request) {
        MonitorAlarmService.apiPlanIncr("v2", accessKey, "all");

        long start = System.currentTimeMillis();
        request.setAccessKey(accessKey);

        try {
            defenseService.checkParams(request);
        } catch (Exception e) {
            MonitorAlarmService.apiPlanIncr("v2", accessKey, "check_params_fail");
            if (e instanceof ExceptionWithCode) {
                ExceptionWithCode ex = (ExceptionWithCode) e;
                return doLogV2(request, ResponseMessage.fail(ex.getCode(), ex.getMessage(), null), start);
            } else {
                return doLogV2(request, ResponseMessage.fail(e.getMessage(), null), start);
            }
        }

        request.setAccessTarget(ConstantVals.TARGET_DEFENSE_V2);
        try {
            if (defenseService.rateLimited(request)) {
                MonitorAlarmService.apiPlanIncr("v2", accessKey, "rate_limited");
                return doLogV2(request, ResponseMessage.fail(ResponseCode.rate_limited.code, "触发限流", null), start);
            }
        } catch (Exception e) {
            MonitorAlarmService.apiPlanIncr("v2", accessKey, "rate_limit_check_fail");
            if (e instanceof ExceptionWithCode) {
                ExceptionWithCode ex = (ExceptionWithCode) e;
                return doLogV2(request, ResponseMessage.fail(ex.getCode(), ex.getMessage(), null), start);
            } else {
                return doLogV2(request, ResponseMessage.fail(e.getMessage(), null), start);
            }
        }

        try {
            if (!accessKey.startsWith("test")) {
                defenseService.verify(request);
            }
            logger.debug("原始请求数据: {}", JSON.toJSONString(request));
            defenseService.preProcess(request);
            if (StringUtils.isEmpty(request.getContent())) {
                logger.warn("预处理后待检测内容为空，req={}", JSON.toJSONString(request));
                return doLogV2(request, ResponseMessage.success("ok", Lists.newArrayList()), start);
            }

        } catch (Exception e) {
            MonitorAlarmService.apiPlanIncr("v2", accessKey, "auth_fail");
            if (e instanceof ExceptionWithCode) {
                ExceptionWithCode ex = (ExceptionWithCode) e;
                return doLogV2(request, ResponseMessage.fail(ex.getCode(), ex.getMessage(), null), start);
            } else {
                return doLogV2(request, ResponseMessage.fail(ResponseCode.param_invalid.code, e.getMessage(), null), start);
            }
        }

        List<DefenseApiResponse> responseData = null;
        ResponseMode mode = request.getResponseMode();
        try {
            switch (mode) {
                case sync:
                    // 同步执行时也返回`未返回的已检测结果`
                    responseData = defenseService.process(request);
                    if (CollectionUtils.isEmpty(responseData)) {
                        return doLogV2(request, ResponseMessage.fail(ResponseCode.fail.code, "识别结果为空", null), start);
                    }

                    break;
                case free_taxi:
                    defenseService.submit(request);
                    responseData = defenseService.fetchResult(request.getAccessKey(), request.getMessageInfo().getSessionId(), DefenseResultFetchRequest.Type.all);
                    if (CollectionUtils.isNotEmpty(responseData) && request.getMessageInfo().getSliceId() != null) {
                        responseData = responseData.stream().filter(x ->
                                        Objects.equals(request.getMessageInfo().getMessageId(), x.getRequests().get(0).getMessageId())
                                )
                                .collect(Collectors.toList());
                    }

                    // todo 后续关注
                    // responseData = ContextHelper.filter(request.getMessageInfo().getFromRole(), responseData);
                    break;
                case mq:
                case http:
                    defenseService.submit(request);
                    responseData = Lists.newArrayList();
                    break;
                default:
                    // should be here
                    return doLogV2(request, ResponseMessage.fail(ResponseCode.fail.code, "不存在的reponseMode", null), start);
            }
        } catch (Exception e) {
            MonitorAlarmService.apiPlanIncr("v2", accessKey, "process_fail");
            logger.error("处理请求异常，req={}", JSON.toJSONString(request), e);
            if (e instanceof ExceptionWithCode) {
                ExceptionWithCode ex = (ExceptionWithCode) e;
                return doLogV2(request, ResponseMessage.fail(ex.getCode(), ex.getMessage(), null), start);
            } else {
                return doLogV2(request, ResponseMessage.fail(ResponseCode.fail.code, e.getMessage(), null), start);
            }
        }


        MonitorAlarmService.apiPlanIncr("v2", accessKey, "success");
        return doLogV2(request, ResponseMessage.success("ok", responseData), start);
    }

    private ResponseMessage doLogV2(Object req, ResponseMessage response, long start)  {
        return doLog("防御V2", req, response, start);
    }

    private ResponseMessage doLogV2Fetch(Object req, ResponseMessage response, long start)  {
        return doLog("防御V2fetch", req, response, start);
    }

    private ResponseMessage doLog(String prefix, Object req, ResponseMessage response, long start)  {
        response.cost(System.currentTimeMillis() - start);
        logger.info("{} >> request={}, response={}", prefix, JSON.toJSONString(req), JSON.toJSONString(response));
        return response;
    }


    @PostMapping("/defenseResult/v2/{accessKey}")
    public Object defenseResultV1(@PathVariable String accessKey,
                                  @RequestBody DefenseResultFetchRequest request) {
        long start = System.currentTimeMillis();
        request.setAccessKey(accessKey);
        if (StringUtils.isEmpty(request.getSessionId())) {
            return doLogV2Fetch(request, ResponseMessage.fail("sessionId为空", null), start);
        }

        try {
            defenseService.verify(request);
        } catch (Exception e) {
            return  doLogV2Fetch(request, ResponseMessage.fail(ResponseCode.param_invalid.code, e.getMessage(), null), start);
        }

        try {
            List<DefenseApiResponse> defenseApiResponses = defenseService.fetchResult(request);
            return  doLogV2Fetch(request, ResponseMessage.success("ok", defenseApiResponses), start);

        } catch (ExceptionWithCode e) {
            return  doLogV2Fetch(request, ResponseMessage.fail(e.getCode(), e.getMessage(), null), start);
        }
    }
}
