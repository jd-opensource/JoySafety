package com.jd.security.llmsec.service.execute.functions;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.check.KbSearchCheck;
import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.conf.funcs.KbSearchConf;
import com.jd.security.llmsec.core.conf.funcs.RagAnswerConf;
import com.jd.security.llmsec.core.engine.AbstractFunction;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;
import com.jd.security.llmsec.service.ScheduleWarmup;
import com.jd.security.llmsec.utils.HttpHelper;
import com.jd.security.llmsec.utils.JsonUtils;
import okhttp3.OkHttpClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;



public class KbSearchFunction extends AbstractFunction {
    private static Logger logger = LoggerFactory.getLogger(KbSearchFunction.class);

    public KbSearchFunction(FunctionConfig functionConf) throws Exception {
        super(functionConf);
    }


    private KbSearchConf conf;
    private OkHttpClient client;
    @Override
    protected void doInit(Map<String, Object> conf) throws Exception {
        KbSearchConf tConf = JsonUtils.obj2obj(conf, KbSearchConf.class);
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
                            HttpHelper.post(client, tConf.getUrl(), HttpHelper.keepaliveHeader(), param, new TypeReference<ResponseMessage<List<KbSearchConf.Resp>>>(){});
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
        KbSearchConf.Req param = new KbSearchConf.Req();
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
            ResponseMessage<List<KbSearchConf.Resp>> responseMessage = HttpHelper.post(client, this.conf.getUrl(), HttpHelper.keepaliveHeader(), param, new TypeReference<ResponseMessage<List<KbSearchConf.Resp>>>() {
            });
            if (responseMessage == null || !responseMessage.success()) {
                logger.error("请求知识库失败，req={}", JSON.toJSONString(param));
                return null;
            }

            List<KbSearchConf.Resp> data = responseMessage.getData();
            if (CollectionUtils.isEmpty(data)) {
                logger.warn("请求知识库结果为空，req={}", JSON.toJSONString(param));
                return null;
            }

            List<KbSearchCheck.Doc> docs = convert(param, data);
            docs = filter(docs);
            if (CollectionUtils.isEmpty(docs)) {
                logger.info("请求知识库结果过滤后为空，req={}，resp={}", JSON.toJSONString(param), JSON.toJSONString(docs));
                KbSearchCheck result = new KbSearchCheck();
                result.setRiskCode(0);
                result.setRiskMessage("未匹配红线问题");
                result.setDocs(docs);
                return result;
            }

            KbSearchCheck result = new KbSearchCheck();
            // todo 外部传入定义的code
            result.setRiskCode(110100);
            result.setRiskMessage("红线知识参考");
            result.setDocs(docs);
            return result;
        } catch (IOException e) {
            logger.error("请求知识库失败，req={}", JSON.toJSONString(param), e);
        }
        return null;
    }

    private List<KbSearchCheck.Doc> filter(List<KbSearchCheck.Doc> docs) {
        return docs.stream().filter(x -> x.getScore() > conf.getThreshold()).collect(Collectors.toList());
    }

    public static List<KbSearchCheck.Doc> convert(Object param, List<RagAnswerConf.Resp> data) {
        List<KbSearchCheck.Doc> ret = Lists.newArrayList();
        // 不考虑批量请求的情况
        RagAnswerConf.Resp resp = data.get(0);
        List<String> ids = resp.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return ret;
        }

        List<Double> scores = resp.getScores();
        List<String> textList = resp.getText_list();
        List<JSONObject> metadatas = resp.getMetadatas();

        int size = ids.size();
        for (int i = 0; i < size; i++) {
            KbSearchCheck.Doc doc = new KbSearchCheck.Doc();
            doc.setId(ids.get(i));
            doc.setScore(scores.get(i));
            doc.setText(textList.get(i));
            doc.setMetadata(metadatas.get(i));
            ret.add(doc);
        }

        return ret;
    }

    @Override
    public RiskCheckType type() {
        return RiskCheckType.kb_search;
    }
}
