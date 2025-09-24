package com.jd.security.llmsec.service.execute;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jd.security.llmsec.config.GlobalConf;
import com.jd.security.llmsec.core.BusinessConf;
import com.jd.security.llmsec.core.ResponseCode;
import com.jd.security.llmsec.core.api.defense.AB;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.check.*;
import com.jd.security.llmsec.core.engine.FunctionChainBuilder;
import com.jd.security.llmsec.core.engine.FunctionExecutor;
import com.jd.security.llmsec.core.engine.Node;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.openai.Message;
import com.jd.security.llmsec.core.session.*;
import com.jd.security.llmsec.service.ConfigService;
import com.jd.security.llmsec.service.MonitorAlarmService;
import com.jd.security.llmsec.service.common.ThreadLocalService;
import com.jd.security.llmsec.service.session.SessionService;
import com.jd.security.llmsec.service.storage.MessageQueueService;
import com.jd.security.llmsec.service.storage.StorageService;
import com.jd.security.llmsec.utils.BertUtils;
import com.jd.security.llmsec.utils.ContentHelper;
import com.jd.security.llmsec.utils.ContextHelper;
import com.jd.security.llmsec.utils.MessageHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;



@Service
public class FunctionExecutorImpl implements FunctionExecutor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("DynamicConfigService")
    private ConfigService configService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private GlobalConf globalConf;

    @Override
    public List<Pair> invoke(List<DefenseApiRequest> requests) throws ExceptionWithCode {
        // todo 最长检测文本的限制&长文本解决方案
        List<List<DefenseApiRequest>> splits = splitByRole(requests);
        List<Pair> result = Lists.newArrayList();
        for (List<DefenseApiRequest> reqs : splits) {
            Pair pair = doInvoke(reqs);
            if (pair != null) {
                result.add(pair);
            } else {
                logger.error("异步识别返回结果为空，req={}", JSON.toJSONString(reqs, SerializerFeature.DisableCircularReferenceDetect));
            }
        }

        return result;
    }

    // group by from role
    private List<List<DefenseApiRequest>> splitByRole(List<DefenseApiRequest> requests) {
        List<List<DefenseApiRequest>> result = Lists.newArrayList();
        List<DefenseApiRequest> tmp = Lists.newArrayList();
        Role curRole = requests.get(0).getMessageInfo().getFromRole();
        for (DefenseApiRequest req : requests) {
            if (Objects.equals(curRole, req.getMessageInfo().getFromRole())) {
                tmp.add(req);
            } else {
                result.add(tmp);
                tmp = Lists.newArrayList();
                curRole = req.getMessageInfo().getFromRole();
            }
        }

        if (CollectionUtils.isNotEmpty(tmp)) {
            result.add(tmp);
        }
        return result;
    }

    @Autowired
    private MessageQueueService messageQueueService;

    @Autowired
    private StorageService storageService;

    @Override
    public DefenseApiResponse invoke(DefenseApiRequest request) throws ExceptionWithCode {
        MessageInfo messageInfo = request.getMessageInfo();
        String sessionId = messageInfo.getSessionId();
        Integer messageId = messageInfo.getMessageId();
        boolean shouldLock = StringUtils.isNotEmpty(sessionId) && messageId != null;
        if (globalConf.openaiRequestBizs().contains(request.getAccessKey()) && request.fromUser()) {
            shouldLock = false;
        }
        boolean lockSuccess = false;
        if (shouldLock) {
            for (int i = 0; i < 6; i++) {
                try {
                    lockSuccess = storageService.lock(ContentHelper.lockKey(sessionId), 2);
                    if (lockSuccess) {
                        logger.info("获取识别结果，i={}", i);
                        break;
                    }
                    try {
                        logger.info("等待获取识别结果，i={}", i);
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException ex) {
                        // ignore
                    }
                } catch (Exception e) {
                    logger.error("获取锁失败，i={}", i, e);
                }
            }
        }

        try {
            Pair pair = doInvoke(Lists.newArrayList(request));
            if (pair != null) {
//                try {
//                    messageQueueService.appendResponseData(pair.getContext(), request.getAccessKey(), sessionId, pair.getResponse());
//                } catch (Exception e) {
//                    logger.error("同步识别，消息写jdq失败", e);
//                }

                return pair.getResponse();
            } else {
                logger.error("同步识别返回结果为空，req={}", JSON.toJSONString(request));
                return null;
            }
        } finally {
            if (shouldLock && lockSuccess) {
                try {
                    storageService.unlock(ContentHelper.lockKey(sessionId));
                } catch (Exception e) {
                    logger.error("解锁失败", e);
                }
            }
        }
    }

    private Pair doInvoke(List<DefenseApiRequest> reqs) throws ExceptionWithCode {
        for (DefenseApiRequest req : reqs) {
            com.alibaba.fastjson2.JSONObject ext = req.getMessageInfo().getExt();
            if (ext == null) {
                ext = new com.alibaba.fastjson2.JSONObject();
            }
            ext.put("_process_node_", GlobalConf.localIp + "/" + Thread.currentThread().getName());
            ext.put("_process_time_", System.currentTimeMillis());
        }

        SessionContext context;
        long start = System.currentTimeMillis();
        try {
            context = buildContext(reqs);
        } catch (Exception e) {
            logger.error("构建上下文失败", e);
            throw new ExceptionWithCode(ResponseCode.inner_error.code, "构建上下文失败:" + e.getMessage());
        }

        if (context == null) {
            return null;
        }

        RiskCheckResult curResult = null;
        boolean hitCache = false;
        boolean shouldCache = shouldCache(context);
        if (shouldCache) {
            curResult = loadCache(context);
            context.setCurResult(curResult); // 主要用于打日志，最终把数据同步到kibana
            if (curResult != null) {
                hitCache = true;
            }
        }

        if (curResult == null) {
            curResult = doExecute(reqs, context);
            if (shouldCache && curResult != null) {
                updateCache(context, curResult);
            }
        }

        DefenseApiRequest firstReq = reqs.get(0);
        if (curResult != null) {
            DefenseApiResponse response = ContextHelper.buildResp(reqs, curResult, context);
            try {
                if (!globalConf.historyIgnore(context.getBusinessConf().getName(), context)) {
                    saveToHistory(context, context.getBusinessConf().getAccessKey(), firstReq.getMessageInfo().getSessionId(), reqs, response);
                }
            } catch (Exception e) {
                logger.error("保存识别结果异常, context={}", JSON.toJSONString(context), e);
                MonitorAlarmService.requestIncr(context.getBusinessConf().getName(), "store_history_error");
            }

            long end = System.currentTimeMillis();
            context.setRecvExecuteCost(end - reqs.get(0).getTimestamp());
            context.setExecuteCost(end - start);
            context.setExecuteEndTime(end);
            if (hitCache) {
                context.setEndReason("hitCache");
            } else if (StringUtils.isNotEmpty(context.getEndReason())) {
                context.setEndReason("finish");
            }
            String contentLog = ContextHelper.fullLog(context);
            try {
                String sessionId = firstReq.getMessageInfo().getSessionId();
                if (StringUtils.isEmpty(sessionId)) {
                    sessionId = String.valueOf(System.currentTimeMillis());
                }
                messageQueueService.appendContextData(firstReq.getAccessKey(), sessionId, contentLog);
            } catch (Exception e) {
                logger.error("同步识别，消息写jdq失败", e);
            }

            if (response != null && response.hasRisk()) {
                BusinessConf businessConf = context.getBusinessConf();
                MonitorAlarmService.riskIncr(businessConf.getName(), String.valueOf(response.getRiskCode()));
            }
            return new Pair(context, reqs, response);
        } else {
            MonitorAlarmService.requestIncr(context.getBusinessConf().getName(), "null_execute_result");
            return null;
        }
    }

    private final int RESP_CACHE_SECONDS = 15 * 60;

    private void updateCache(SessionContext context, RiskCheckResult curResult) {
        String businessName = context.getBusinessConf().getName();
        String checkContent = context.getCheckContent();
        try {
            storageService.execute(jedis -> jedis.set(ContentHelper.respCacheKey(businessName, checkContent), JSON.toJSONString(curResult),
                    "NX", "EX", RESP_CACHE_SECONDS + (System.currentTimeMillis() % 300)));
        } catch (Exception e) {
            logger.error("缓存失败, context={}", JSON.toJSONString(context), e);
        }
    }

    private RiskCheckResult loadCache(SessionContext context) {
        String businessName = context.getBusinessConf().getName();
        String checkContent = context.getCheckContent();
        try {
            String cachedJson = (String) storageService.execute(jedis -> jedis.get(ContentHelper.respCacheKey(businessName, checkContent)));
            return ContextHelper.convert(cachedJson);
        } catch (Exception e) {
            logger.error("获取缓存失败, context={}", JSON.toJSONString(context), e);
            return null;
        }
    }

    @Value("${execute.cache.enable}")
    private boolean cacheEnable;

    private boolean shouldCache(SessionContext context) {
        if (!cacheEnable) {
            return false;
        }

        boolean fromUser = context.getCurReq().get(0).fromUser();
        if (!fromUser) {
            return false;
        }

        CheckConf userCheckConf = context.getBusinessConf().getUserCheckConf();
        return userCheckConf.getCheckNum() == 1;
    }

    private RiskCheckResult doExecute(List<DefenseApiRequest> reqs, SessionContext context) throws ExceptionWithCode {
        Node root = root(context.getBusinessConf().getAccessKey(), reqs.get(0).getAb());
        if (root == null) {
            throw new ExceptionWithCode(ResponseCode.inner_error.code, "未找到对应的执行node");
        }

        Node curNode = root;
        while (curNode != null) {
            if (context.timeout()) {
                logger.error("执行超时，当前上下文：{}", JSON.toJSONString(context));
                throw new ExceptionWithCode(ResponseCode.exec_timeout.code, ResponseCode.exec_timeout.description);
            }

            context.setCurResult(null);
            context.setCurFunctionName(null);

            try {
                RiskCheckResult tmpResult;
                tmpResult = curNode.run(context);
                context.setCurResult(tmpResult);
                context.setCurFunctionName(curNode.getFunction().name());
            } catch (Exception e) {
                MonitorAlarmService.functionIncr(context.getBusinessConf().getAccessKey(), curNode.getFunction().name(), e.getClass().getName());
                if (!curNode.getConf().isIgnoreError()) {
                    logger.error("node执行异常，拋出异常, context={}", JSON.toJSONString(context, SerializerFeature.DisableCircularReferenceDetect), e);
                    if (e instanceof ExceptionWithCode) {
                        throw (ExceptionWithCode) e;
                    } else {
                        throw new ExceptionWithCode(ResponseCode.inner_error.code, e.getMessage());
                    }
                } else {
                    logger.error("node执行异常，异常忽略, context={}", JSON.toJSONString(context, SerializerFeature.DisableCircularReferenceDetect), e);
                    context.setCurResult(null);
                    context.setCurFunctionName(curNode.getFunction().name());
                }
            }

//            if (!curNode.hasNext(context)) {
//                break;
//            }
            curNode = curNode.next(context);
            if (curNode == null) {
                break;
            }
        }

        RiskCheckResult curResult = context.getCurResult();

        if (curResult instanceof KbSearchCheck || curResult instanceof RagAnswerCheck) {
            buildOriginCheck(context, curResult);
        }

        if (curResult != null && StringUtils.isNotEmpty(curResult.getRiskMessage())
                && !Character.isDigit(curResult.getRiskMessage().charAt(0))) {
            curResult.setRiskMessage(curResult.getRiskCode() + curResult.getRiskMessage());
        }

        return curResult;
    }

    private static void buildOriginCheck(SessionContext context, RiskCheckResult risk) {
        SingleLabelPredCheck originCheck = null;
        if (context.getMiddleResults() != null) {
            List<RiskCheckResult> singleLabelPreds = context.getMiddleResults().get(RiskCheckType.single_label_pred);
            if (CollectionUtils.isNotEmpty(singleLabelPreds)) {
                originCheck = (SingleLabelPredCheck) singleLabelPreds.get(singleLabelPreds.size() - 1);
            }
        }

        if (originCheck != null) {
            if (risk instanceof KbSearchCheck) {
                ((KbSearchCheck) risk).setOriginCheck(originCheck);
            }

            if (risk instanceof RagAnswerCheck) {
                ((RagAnswerCheck) risk).setOriginCheck(originCheck);
            }
        }
    }

    private void saveToHistory(SessionContext context, String businessName, String sessionId, List<DefenseApiRequest> defenseApiRequests, DefenseApiResponse response) throws Exception {
        if (CollectionUtils.isEmpty(defenseApiRequests) || response == null) {
            return;
        }

        long now = System.currentTimeMillis();
        List<History> histories = Lists.newArrayList();
        boolean fromRobot = defenseApiRequests.get(0).fromRobot();
        History<DefenseApiRequest> requestHistory = new History<>();
        requestHistory.setData(defenseApiRequests);
        requestHistory.setTimestamp(now);
        requestHistory.setType(fromRobot ? HistoryType.robot : HistoryType.user);
        histories.add(requestHistory);

// 历史中不再放中间识别结果
//        History<DefenseApiResponse> responseHistory = new History<>();
//        responseHistory.setType(HistoryType.system);
//        responseHistory.setData(Lists.newArrayList(response));
//        responseHistory.setTimestamp(now);
//        histories.add(responseHistory);

        sessionService.addHistory(context, businessName, sessionId, histories);
    }

    @Autowired
    private FunctionChainBuilder functionChainBuilder;
    @Autowired
    private ThreadLocalService threadLocalService;

    private final int MAX_CHECK_LEN = 2048;

    private Node root(String accessKey, AB ab) {
        return functionChainBuilder.root(accessKey, ab);
    }

    private SessionContext buildContext(List<DefenseApiRequest> requests) throws Exception {
        if (CollectionUtils.isEmpty(requests)) {
            return null;
        }

        DefenseApiRequest firstReq = requests.get(0);
        BusinessConf businessConf = configService.businessConf(firstReq);
        Preconditions.checkArgument(businessConf != null, "业务不存在");
        MessageInfo messageInfo = firstReq.getMessageInfo();

        SessionContext context = new SessionContext();
        context.setBusinessConf(businessConf);

        context.setExecuteStartTime(System.currentTimeMillis());
        List<History> histories = buildHistories(requests, messageInfo, firstReq, context, businessConf);
        context.setHistories(histories);
        context.setCurReq(requests);
        if (CollectionUtils.isNotEmpty(firstReq.getOpenaiMessages())) {
            context.setCheckContent(MessageHelper.userContent(firstReq.getOpenaiMessages()));
        } else {
            context.setCheckContent(ContentHelper.join2NormalStrWithOrder(context));
        }

        String checkContent = context.getCheckContent();
        if (checkContent != null && checkContent.length() > MAX_CHECK_LEN) {
            checkContent = checkContent.substring(0, MAX_CHECK_LEN / 2) + "_c_" + checkContent.substring(checkContent.length() - MAX_CHECK_LEN / 2);
            context.setCheckContent(checkContent);
        }

        return context;
    }

    private @Nullable List<History> buildHistories(List<DefenseApiRequest> requests, MessageInfo messageInfo, DefenseApiRequest firstReq, SessionContext context, BusinessConf businessConf) throws Exception {
        List<Message> openaiMessages = firstReq.getOpenaiMessages();
        if (CollectionUtils.isNotEmpty(openaiMessages)) {
            return convert(openaiMessages);
        }
        List<History> histories = Lists.newArrayList();

        CheckUnit checkUnit = null;
        CheckConf checkConf = firstReq.fromRobot() ? businessConf.getRobotCheckConf() : businessConf.getUserCheckConf();
        if (checkConf != null && checkConf.getUnit() != null) {
            checkUnit = checkConf.getUnit();
        }

        if (checkUnit != null) {
            // 条件不满足，多条检测退化到单条
            if (Objects.equals(checkUnit, CheckUnit.message_multi) && StringUtils.isEmpty(messageInfo.getSessionId())) {
                checkUnit = CheckUnit.message_single;
                // 条件不满足，多条检测退化到单分片
            } else if (Objects.equals(checkUnit, CheckUnit.slice_multi) && (StringUtils.isEmpty(messageInfo.getSessionId()) || messageInfo.getMessageId() == null)) {
                checkUnit = CheckUnit.slice_single;
            }
        } else {
            checkUnit = firstReq.getMessageInfo().getSliceId() != null ? CheckUnit.slice_single : CheckUnit.message_single;
        }

        int checkNum = checkConf != null && checkConf.getCheckNum() > 0 ? checkConf.getCheckNum() : 1;
        int extraFetchNum = 1;
        if (checkNum == 1) {
            extraFetchNum = 0;
        } else {
            extraFetchNum = checkNum - requests.size();
            if (extraFetchNum <= 0) {
                // 避免漏掉边界
                extraFetchNum = 1;
            }
        }

        if (StringUtils.isNotEmpty(messageInfo.getSessionId())) {
            switch (checkUnit) {
                case slice_single:
                    break;
                case slice_multi:
                    if (messageInfo.getMessageId() != null && extraFetchNum > 0) {
                        histories = sessionService.latestSlice(firstReq.getAccessKey(), messageInfo.getSessionId(), messageInfo.getMessageId(), extraFetchNum, firstReq.fromUser(), context);
                    }
                    break;
                case message_single:
                    break;
                case message_multi:
                    if (messageInfo.getMessageId() != null) {
                        histories = sessionService.latestMessage(firstReq.getAccessKey(), messageInfo.getSessionId(), checkConf.getCheckNum() - 1, firstReq.fromUser(), context);
                    }
                    break;
            }
        }

//        if (firstReq.fromRobot()) {
//            DefenseApiRequest lastUserReq = threadLocalService.lastUserReq.get();
//            if (lastUserReq != null) {
//                context.setLastUserReq(lastUserReq);
//            }
//        }
        boolean haveStop = false;
        for (DefenseApiRequest r : requests) {
            for (String end : BertUtils.fullSentenceEnds) {
                if (r.getContent().contains(end)) {
                    haveStop = true;
                    break;
                }
            }
            if (haveStop) {
                break;
            }
        }

        boolean fullSentenceCheck = haveStop && firstReq.fromRobot() && Objects.equals(ResponseMode.free_taxi, firstReq.getResponseMode());
        if (CollectionUtils.isNotEmpty(histories) && checkConf != null) {
            histories = reduceByCheckNum(histories, fullSentenceCheck ? BertUtils.BERT_LEN : extraFetchNum);
            logger.debug("reduce会话历史，sessionId={}, messageId={}, histories={}", firstReq.getMessageInfo().getSessionId(), firstReq.getMessageInfo().getMessageId(), JSON.toJSONString(histories));
        }
        return histories;
    }

    private List<History> convert(List<Message> openaiMessages) {
        List<History> ret = Lists.newLinkedList();
        int msgId = 1;
        for (Message msg : openaiMessages) {
            History history = convert(msg, msgId);
            if (history != null) {
                ret.add(history);
                msgId++;
            }
        }
        return ret;
    }

    private History convert(Message msg, int msgId) {
        History h = new History();
        h.setTimestamp(System.currentTimeMillis());
        DefenseApiRequest req = new DefenseApiRequest();
        req.setContent(msg.getContent());
        MessageInfo messageInfo = new MessageInfo();
        req.setMessageInfo(messageInfo);

        messageInfo.setMessageId(msgId);
        com.jd.security.llmsec.core.openai.Role role = msg.getRole();
        if (role == null) {
            return null;
        }
        switch (role) {
            case system:
                return null;
            case user:
                messageInfo.setFromRole(Role.user);
                break;
            case assistant:
                messageInfo.setFromRole(Role.robot);
                break;
        }

        h.setData(Lists.newArrayList(req));
        return h;
    }

    private List<History> reduceByCheckNum(List<History> histories, int num) {
        if (num <= 0) {
            return Lists.newArrayList();
        }
        List<History> ret = Lists.newArrayList();
        // 留点buffer，减少乱序造成的影响
        num += 5;
        int cnt = 0;
        for (int i = 0; i < histories.size(); i++) {
            History h = histories.get(histories.size() - 1 - i);
            if (cnt >= num) {
                break;
            }
            if (h.getData() != null && h.getData().size() > 0) {
                ret.add(h);
                cnt += h.getData().size();
            }
        }
        return ret;
    }
}
