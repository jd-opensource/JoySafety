package com.jd.security.llmsec.service.execute.functions;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Maps;
import com.jd.security.llmsec.core.ResponseCode;
import com.jd.security.llmsec.core.check.KeywordCheck;
import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.conf.funcs.KeywordConf;
import com.jd.security.llmsec.core.engine.AbstractFunction;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.rpc.KeywordApiService;
import com.jd.security.llmsec.core.session.SessionContext;
import com.jd.security.llmsec.utils.HttpHelper;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;



public class KeywordFunction extends AbstractFunction {
    private static Logger logger = LoggerFactory.getLogger(KeywordFunction.class);

    public KeywordFunction(FunctionConfig functionConf) throws Exception {
        super(functionConf);
    }

    @Override
    public RiskCheckType type() {
        return RiskCheckType.keyword;
    }

    private KeywordConf conf;
    private OkHttpClient client;
    @Override
    protected void doInit(Map<String, Object> conf) {
        this.conf = JSON.parseObject(JSON.toJSONString(conf), KeywordConf.class);
        if (StringUtils.isEmpty(this.conf.getUrl())) {
            throw new IllegalArgumentException("url为空");
        }
        long timeout = 200;
        if (this.conf().getTimeoutMilliseconds() != null) {
            timeout = this.conf().getTimeoutMilliseconds();
        }

        KeywordConf thisConf = this.conf;

        Object resource = FunctionResourcePool.getFunc(type(), thisConf.getUrl());
        if (resource != null) {
            this.client = (OkHttpClient) resource;
        } else {
            OkHttpClient temp = new OkHttpClient.Builder().callTimeout(timeout, TimeUnit.MILLISECONDS).build();
            Object uniqResource = FunctionResourcePool.addFuncIfAbsent(type(), thisConf.getUrl(), temp);
            this.client = (OkHttpClient) uniqResource;
        }
    }

    @Override
    public RiskCheckResult doRun(SessionContext context) throws ExceptionWithCode {
        KeywordApiService.KVResponse result = null;
        String bizName = context.getBusinessConf().getName();


        Map<String, String> param = Maps.newHashMap();
        param.put("content", context.getCheckContent());
        param.put("businessName", bizName);

        HashMap<String, String> headers = Maps.newHashMap();
        headers.put("Content-type", "application/json");
        headers.put("Connection", "Keep-Alive");
        // 20秒内/发达5000个请求后关闭连接
        headers.put("Keep-Alive", "timeout=20, max=5000");

        try {
            result = HttpHelper.post(this.client, conf.getUrl(), headers, param, KeywordApiService.KVResponse.class);
            if (result == null || !result.success()) {
                logger.error("调用关键词接口失败, req={}, response={}", JSON.toJSONString(param), JSON.toJSONString(result));
                return null;
            }
        } catch (IOException e) {
            logger.error("调用关键词接口失败", e);
            throw new ExceptionWithCode(ResponseCode.inner_error.code, e.getMessage());
        }

        return KeywordCheck.keywordCheckBuilder()
                .riskCode(result.getFirstClassNo())
                .riskMessage(result.getFirstClassName())
                .firstClassNo(result.getFirstClassNo())
                .firstClassName(result.getFirstClassName())
                .hitWord(result.getHitWord())
                .bwgLabel(result.getBwgLabel())
                .latency((int) result.getLatency())
                .build();
    }
}
