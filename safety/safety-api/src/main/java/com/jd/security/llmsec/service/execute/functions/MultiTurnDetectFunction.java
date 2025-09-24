package com.jd.security.llmsec.service.execute.functions;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jd.security.llmsec.core.BusinessConf;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.check.MultiTurnDetectCheck;
import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.conf.funcs.MultiTurnDetectConf;
import com.jd.security.llmsec.core.conf.funcs.MultiTurnDetectConf.Req;
import com.jd.security.llmsec.core.engine.AbstractFunction;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.openai.Message;
import com.jd.security.llmsec.core.openai.Role;
import com.jd.security.llmsec.core.session.History;
import com.jd.security.llmsec.core.session.MessageInfo;
import com.jd.security.llmsec.core.session.SessionContext;
import com.jd.security.llmsec.service.BeanBridge;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;



public class MultiTurnDetectFunction extends AbstractFunction {
    private static Logger logger = LoggerFactory.getLogger(MultiTurnDetectFunction.class);
    public MultiTurnDetectFunction(FunctionConfig functionConf) throws Exception {
        super(functionConf);
    }

    private MultiTurnDetectConf conf;
    private OkHttpClient client;
    @Override
    protected void doInit(Map<String, Object> conf) throws Exception {
        MultiTurnDetectConf tConf = JsonUtils.obj2obj(conf, MultiTurnDetectConf.class);
        Preconditions.checkNotNull(tConf, "配置为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(tConf.getUrl()), "url为空");
        Preconditions.checkArgument(tConf.getMaxTurns() != null && tConf.getMaxTurns() > 0, "maxTurns > 0");
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

                        Req param = new Req();
                        param.setRequest_id(String.valueOf(System.currentTimeMillis()));
                        param.setBusiness_id("test");
                        param.setSession_id(param.getRequest_id());
                        Message m = new Message(Role.user, "你好");
                        param.setHistory(Lists.newArrayList(m));

                        try {
                            HttpHelper.post(client, tConf.getUrl(), HttpHelper.keepaliveHeader(), param, new TypeReference<ResponseMessage<List<MultiTurnDetectCheck>>>(){});
                        } catch (IOException e) {
                            logger.warn("{}/{} warmup error", funcConf.getType(), funcConf.getName(), e);
                        }
                    }
                });
            }
        }
    }

    private final static int MAX_FETCH_HISTORIES = 200;
    @Override
    protected RiskCheckResult doRun(SessionContext context) throws ExceptionWithCode {
        DefenseApiRequest firstReq = context.getCurReq().get(0);
        MessageInfo messageInfo = firstReq.getMessageInfo();
        BusinessConf businessConf = context.getBusinessConf();
        Req param = new Req();
        param.setRequest_id(firstReq.getRequestId());
        param.setBusiness_id(businessConf.getAccessKey());
        param.setSession_id(messageInfo.getSessionId());
        List<Message> messages = convert(context.getHistories(), context.getCurReq());
        if (messages.size() < conf.getMaxTurns() * 2) {
            try {
                List<History> histories = BeanBridge.gSessionService.latestMessageWithoutCache(businessConf.getAccessKey(), messageInfo.getSessionId(), MAX_FETCH_HISTORIES, firstReq.fromUser());
                messages = convert(histories, context.getCurReq());
                int tSize = messages.size();
                if (tSize > conf.getMaxTurns() * 2) {
                    messages = messages.subList(tSize - conf.getMaxTurns() * 2, tSize);
                }
            } catch (Exception e) {
                logger.error("获取上下文信息失败, ctx={}", JSON.toJSONString(context), e);
            }
        }

        param.setHistory(messages);

        try {
            ResponseMessage<MultiTurnDetectCheck> responseMessage = HttpHelper.post(client, conf.getUrl(), HttpHelper.keepaliveHeader(), param, new TypeReference<ResponseMessage<MultiTurnDetectCheck>>() {
            });
            if (responseMessage == null || !responseMessage.success()) {
                logger.error("请求多轮接口失败, req={}, resp={}", JSON.toJSONString(param), JSON.toJSONString(responseMessage));
                return null;
            }

            MultiTurnDetectCheck data = responseMessage.getData();
            if (data == null) {
                logger.error("请求多轮接口失败，数据为空, req={}, resp={}", JSON.toJSONString(param), JSON.toJSONString(responseMessage));
                return null;
            }
            MultiTurnDetectCheck result = new MultiTurnDetectCheck();
            result.setRiskCode(data.getRiskCode());
            result.setRiskMessage(data.getRiskMessage());
            result.setReason(data.getReason());
            result.setCheckedContent(JSON.toJSONString(messages));
            return result;

        } catch (IOException e) {
            logger.error("请求多轮接口失败, req={}", JSON.toJSONString(param), e);
        }
        return null;
    }

    private List<Message> convert(List<History> histories, List<DefenseApiRequest> curReq) {
        List<DefenseApiRequest> allHisReqs = Lists.newArrayList();
        for (History h : histories) {
            allHisReqs.addAll((List<DefenseApiRequest>)h.getData());
        }
        allHisReqs.addAll(curReq);
        List<DefenseApiRequest> mergedReqs = merge(allHisReqs);

        List<Message> hisMessages = Lists.newArrayList();

        for (DefenseApiRequest req : mergedReqs) {
            Role role = req.fromUser() ? Role.user : Role.assistant;
            Message m = new Message(role, req.getContent());
            hisMessages.add(m);
        }

        return hisMessages;
    }

    private List<DefenseApiRequest> merge(List<DefenseApiRequest> allHisReqs) {
        List<DefenseApiRequest> ret = Lists.newArrayList();
        MessageInfo messageInfo = allHisReqs.get(0).getMessageInfo();
        String id = getId(messageInfo);
        List<DefenseApiRequest> temp = Lists.newArrayList(allHisReqs.get(0));
        for (int i = 1; i < allHisReqs.size(); i++) {
            DefenseApiRequest cur = allHisReqs.get(i);
            String newId = getId(cur.getMessageInfo());
            if (Objects.equals(id, newId)) {
                temp.add(cur);
            } else {
                id = newId;
                ret.add(doMerge(temp));
                temp.clear();
                temp.add(cur);
            }
        }

        if (CollectionUtils.isNotEmpty(temp)) {
            ret.add(doMerge(temp));
        }

        return ret;
    }

    private DefenseApiRequest doMerge(List<DefenseApiRequest> temp) {
        String content = temp.stream().map(DefenseApiRequest::getContent).collect(Collectors.joining());
        temp.get(0).setContent(content);
        return temp.get(0);
    }

    private String getId(MessageInfo messageInfo) {
        return messageInfo.getFromRole() + "$" + messageInfo.getMessageId();
    }

    @Override
    public RiskCheckType type() {
        return RiskCheckType.multi_turn_detect;
    }
}
