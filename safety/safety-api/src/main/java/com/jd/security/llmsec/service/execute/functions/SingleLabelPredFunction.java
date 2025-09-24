package com.jd.security.llmsec.service.execute.functions;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.security.llmsec.core.ResponseCode;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.check.RiskInfo;
import com.jd.security.llmsec.core.check.SingleLabelPredCheck;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.conf.funcs.SingleLabelPredConf;
import com.jd.security.llmsec.core.conf.funcs.SingleLabelPredConf.FasttextConf;
import com.jd.security.llmsec.core.engine.AbstractFunction;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;
import com.jd.security.llmsec.service.ScheduleWarmup;
import com.jd.security.llmsec.utils.HttpHelper;
import okhttp3.OkHttpClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;



public class SingleLabelPredFunction extends AbstractFunction {
    private static Logger logger = LoggerFactory.getLogger(SingleLabelPredFunction.class);

    public SingleLabelPredFunction(FunctionConfig functionConf) throws Exception {
        super(functionConf);
    }

    private SingleLabelPredConf conf;
    private FasttextConf fasttextConf;
    private OkHttpClient fasttextClient;
    @Override
    protected void doInit(Map<String, Object> conf) {
        this.conf = JSON.parseObject(JSON.toJSONString(conf), SingleLabelPredConf.class);
        Preconditions.checkArgument(this.conf.getModelType() != null, "modelType is null");
        Preconditions.checkArgument(StringUtils.isNotEmpty(this.conf.getName()), "modelName is null");

        long timeout = 150;
        if (this.conf().getTimeoutMilliseconds() != null) {
            timeout = this.conf().getTimeoutMilliseconds();
        }
        Preconditions.checkArgument(this.conf.getExtra() != null, "extra is null");
        switch (this.conf.getModelType()) {
            case textcnn:
            case llm:
            case bert:
            case fasttext:
                FasttextConf fasttextConf = JSON.parseObject(JSON.toJSONString(this.conf.getExtra()), FasttextConf.class);
                Preconditions.checkArgument(StringUtils.isNotEmpty(fasttextConf.getUrl()), "url is null");
                this.fasttextConf = fasttextConf;

                Object resource = FunctionResourcePool.getFunc(type(), this.fasttextConf.getUrl());
                if (resource != null) {
                    this.fasttextClient = (OkHttpClient) resource;

                } else {
                    Object temp = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.MILLISECONDS).build();
                    Object uniqResource = FunctionResourcePool.addFuncIfAbsent(type(), this.fasttextConf.getUrl(), temp);
                    this.fasttextClient = (OkHttpClient) uniqResource;
                    if (temp == uniqResource) {
                        ScheduleWarmup.appendTask(new Runnable() {
                            @Override
                            public void run() {
                                logger.info("warm up fasttext, url={}", fasttextConf.getUrl());
                                Map<String, Object> param = Maps.newHashMap();
                                param.put("text_list", Collections.singletonList("warmup"));
                                param.put("businessName", "warmup");

                                try {
                                    HttpHelper.post(fasttextClient, fasttextConf.getUrl(), HttpHelper.keepaliveHeader(), param, new TypeReference<ResponseMessage<List<SingleLabelPredCheck.SkillsResp>>>() {});
                                } catch (IOException e) {
                                    logger.warn("fast warmup error", e);
                                }
                            }
                        });
                    }
                }
                break;
        }
    }

    @Override
    public RiskCheckType type() {
        return RiskCheckType.single_label_pred;
    }

    @Override
    public RiskCheckResult doRun(SessionContext context) throws ExceptionWithCode {
        switch (this.conf.getModelType()) {
            case textcnn:
            case llm:
            case bert:
            case fasttext:
                return runFasttext(context);
        }
        return null;
    }


    private SingleLabelPredCheck runFasttext(SessionContext context) throws ExceptionWithCode {
        DefenseApiRequest firstReq = context.getCurReq().get(0);

        SingleLabelPredCheck.SkillsReq req = new SingleLabelPredCheck.SkillsReq();
        req.setBusiness_id(context.getBusinessConf().getName());
        req.setRequest_id(firstReq.getRequestId());
        req.setSession_id(firstReq.getMessageInfo().getSessionId());
        req.setText_list(Lists.newArrayList(context.getCheckContent()));

        try {
            ResponseMessage<List<SingleLabelPredCheck.SkillsResp>> respMessage = HttpHelper.post(this.fasttextClient, fasttextConf.getUrl(), HttpHelper.keepaliveHeader(), req, new TypeReference<ResponseMessage<List<SingleLabelPredCheck.SkillsResp>>>() {});
            if (respMessage == null) {
                return null;
            }

            if (!respMessage.success()) {
                logger.error("调用接口失败, error={}", respMessage.getMessage());
                throw new ExceptionWithCode(ResponseCode.inner_error.code, respMessage.getMessage());
            }

            if (respMessage.getData() == null || CollectionUtils.isEmpty(respMessage.getData())) {
                logger.error("调用接口失败, error=结果为空");
                throw new ExceptionWithCode(ResponseCode.inner_error.code, "结果为空");
            }

            SingleLabelPredCheck.SkillsResp resp = respMessage.getData().get(0);
            SingleLabelPredCheck result = SingleLabelPredCheck.builder()
                    .riskCode(resp.getRiskCode())
                    .riskMessage(resp.getRiskMessage())
                    .probability(resp.getProbability())
                    .build();
            result.setDetail(resp.getDetail());
            doIgnore(result);
            return result;

        } catch (IOException e) {
            logger.error("调用fasttext接口失败", e);
            throw new ExceptionWithCode(ResponseCode.inner_error.code, e.getMessage());
        }
    }

    private void doIgnore(SingleLabelPredCheck result) {
        if (conf.getIgnoreRiskCode().contains(result.getRiskCode())) {
            RiskInfo defaultRisk = conf.getDefaultRisk();
            result.setRiskCode(defaultRisk.getRiskCode());
            result.setRiskMessage(defaultRisk.getRiskMessage());
        }
    }
}
