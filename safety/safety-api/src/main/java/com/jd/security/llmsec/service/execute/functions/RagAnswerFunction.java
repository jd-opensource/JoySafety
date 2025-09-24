package com.jd.security.llmsec.service.execute.functions;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.check.KbSearchCheck;
import com.jd.security.llmsec.core.check.RagAnswerCheck;
import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.conf.funcs.KbSearchConf;
import com.jd.security.llmsec.core.conf.funcs.RagAnswerConf;
import com.jd.security.llmsec.core.conf.funcs.RagAnswerConf.RagResp;
import com.jd.security.llmsec.core.engine.AbstractFunction;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;
import com.jd.security.llmsec.service.ScheduleWarmup;
import com.jd.security.llmsec.utils.HttpHelper;
import com.jd.security.llmsec.utils.JsonUtils;
import lombok.Data;
import okhttp3.OkHttpClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.jd.security.llmsec.service.execute.functions.KbSearchFunction.convert;


public class RagAnswerFunction extends AbstractFunction {
    private static Logger logger = LoggerFactory.getLogger(KbSearchFunction.class);

    public RagAnswerFunction(FunctionConfig functionConf) throws Exception {
        super(functionConf);
    }

    private RagAnswerConf conf;
    private OkHttpClient client;
    @Override
    protected void doInit(Map<String, Object> conf) throws Exception {
        RagAnswerConf tConf = JsonUtils.obj2obj(conf, RagAnswerConf.class);
        Preconditions.checkNotNull(tConf, "配置为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(tConf.getUrl()), "url为空");
        Preconditions.checkArgument(tConf.getTopK() != null && tConf.getTopK() > 0, "topK > 0");
        Preconditions.checkArgument(tConf.getThreshold() != null && tConf.getThreshold() > 0.0, "threshold > 0.0");
        this.conf = tConf;

        FunctionConfig funcConf = super.conf();

        Object resource = FunctionResourcePool.getFunc(type(), this.conf.getUrl());
        if (resource != null) {
            this.client = (OkHttpClient) resource;
        } else {
            Object temp = new OkHttpClient.Builder().callTimeout(funcConf.getTimeoutMilliseconds(), TimeUnit.MILLISECONDS).build();
            Object uniqResource = FunctionResourcePool.addFuncIfAbsent(type(), this.conf.getUrl(), temp);
            this.client = (OkHttpClient) uniqResource;
            if (temp == uniqResource) {
                ScheduleWarmup.appendTask(new Runnable() {
                    @Override
                    public void run() {
                        logger.info("warm up {}/{}, url={}",  funcConf.getType(), funcConf.getName(), tConf.getUrl());

                        KbSearchConf.Req param = new KbSearchConf.Req();
                        param.setCluster(tConf.getCluster());
                        param.setCollection(tConf.getCollection());
                        param.setRequest_id(String.valueOf(System.currentTimeMillis()));
                        param.setBusiness_id("test");
                        param.setSession_id(param.getRequest_id());
                        param.setTop_k(1);
                        param.setText_list(Lists.newArrayList("你好"));

                        try {
                            HttpHelper.post(client, tConf.getUrl(), HttpHelper.keepaliveHeader(), param, new TypeReference<ResponseMessage<List<RagResp>>>(){});
                        } catch (IOException e) {
                            logger.warn("{}/{} warmup error", funcConf.getType(), funcConf.getName(), e);
                        }
                    }
                });
            }
        }
    }

    @Override
    protected RiskCheckResult doRun(SessionContext context) throws ExceptionWithCode {
        RagAnswerConf.Req param = new RagAnswerConf.Req();
        DefenseApiRequest firstReq = context.getCurReq().get(0);
        param.setCluster(this.conf.getCluster());
        param.setCollection(this.conf.getCollection());
        param.setRequest_id(firstReq.getRequestId());
        param.setBusiness_id(context.getBusinessConf().getAccessKey());
        param.setSession_id(firstReq.getMessageInfo().getSessionId());
        param.setTop_k(this.conf.getTopK());
        param.setThreshold(this.conf.getThreshold());
        param.setText_list(Lists.newArrayList(context.getCheckContent()));

        try {
            ResponseMessage<RagResp> responseMessage = HttpHelper.post(client, this.conf.getUrl(), HttpHelper.keepaliveHeader(), param, new TypeReference<ResponseMessage<RagResp>>() {
            });
            if (responseMessage == null || !responseMessage.success()) {
                logger.error("请求代答服务失败，req={}", JSON.toJSONString(param));
                return null;
            }

            RagResp ragResp = responseMessage.getData();
            if (ragResp == null) {
                logger.warn("代答服务返回answer为空，req={}， answer={}", JSON.toJSONString(param), JSON.toJSONString(responseMessage));
                return null;
            }

            String answer = ragResp.getAnswer();
            List<KbSearchCheck.Doc> docs = convert(param, Lists.newArrayList(ragResp.getRefDocs()));
            RagAnswerCheck result = new RagAnswerCheck();
            // todo 外部传入riskcode
            result.setRiskCode(StringUtils.isEmpty(answer) ? 0 : 110101);
            result.setRiskMessage("红线代答");
            result.setAnswer(answer);
            result.setRefDocs(docs);

            return result;
        } catch (IOException e) {
            logger.error("请求知识库失败，req={}", JSON.toJSONString(param), e);
        }
        return null;
    }

    @Override
    public RiskCheckType type() {
        return RiskCheckType.rag_answer;
    }
}
