package com.jd.security.llmsec.service.session;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jd.security.llmsec.config.GlobalConf;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.openai.Message;
import com.jd.security.llmsec.core.openai.Role;
import com.jd.security.llmsec.core.session.History;
import com.jd.security.llmsec.core.session.HistoryType;
import com.jd.security.llmsec.core.session.ResponseMode;
import com.jd.security.llmsec.core.session.SessionContext;
import com.jd.security.llmsec.service.common.ThreadLocalService;
import com.jd.security.llmsec.service.storage.StorageService;
import com.jd.security.llmsec.utils.BertUtils;
import com.jd.security.llmsec.utils.ContentHelper;
import com.jd.security.llmsec.utils.MessageHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Pipeline;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;



@Service
public class SessionServiceImpl implements SessionService {
    private Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);
    @Autowired
    private StorageService storageService;

    @Autowired
    private ThreadLocalService threadLocalService;

    @Autowired
    private GlobalConf globalConf;

    @Override
    public List<History> latestSlice(String businessName, String sessionId, Integer messageId, int checkNum, boolean fromUser, SessionContext context) throws Exception {
        if (StringUtils.isNotEmpty(threadLocalService.sessionId.get()) && CollectionUtils.isNotEmpty(threadLocalService.histories.get())) {
            DefenseApiRequest temp = threadLocalService.lastUserReq.get();
            if (temp != null) {
                context.setLastUserReq(temp);
            }
            return threadLocalService.histories.get();
        }

        List<History> result = latestSliceWithoutCache(businessName, sessionId, messageId, checkNum);
        DefenseApiRequest temp = buildLastUserReq(fromUser, businessName, result);
        if (temp != null) {
            context.setLastUserReq(temp);
        }

        if (StringUtils.isNotEmpty(threadLocalService.sessionId.get())) {
            threadLocalService.histories.set(result);
            if (temp != null) {
                threadLocalService.lastUserReq.set(temp);
            }
        }
        return result;
    }

    private DefenseApiRequest buildLastUserReq(boolean fromUser, String businessName, List<History> histories) {
        if (CollectionUtils.isEmpty(histories)) {
            return null;
        }

        // 以网关为例，一次调用为一个会话，prompt会带有之前请求大模型的所有上文
        if (!fromUser && globalConf.openaiRequestBizs().contains(businessName) && histories.size() == 1) {
            List data = histories.get(0).getData();
            if (data.size() == 1) {
                DefenseApiRequest req = (DefenseApiRequest) data.get(0);
                DefenseApiRequest temp = buildLastUserReqFromOpenai(req);
                if (temp != null) {
                    return temp;
                }
            }
        }

        DefenseApiRequest ret = null;
        for (int i = histories.size() -1; i >= 0; i--) {
            History his = histories.get(0);
            if (Objects.equals(his.getType(), HistoryType.user)) {
                ret = (DefenseApiRequest)his.getData().get(0);
                break;
            }
        }
        return ret;
    }

    private DefenseApiRequest buildLastUserReqFromOpenai(DefenseApiRequest req) {
        List<Message> messages = MessageHelper.parseOpenAIReqMessage(req.getContent());
        if (CollectionUtils.isEmpty(messages)) {
            return null;
        }

        for (int i = messages.size() - 1; i >= 0; i--) {
            if (Objects.equals(messages.get(i).getRole(), Role.user)) {
                String temp = messages.get(i).getContent();
                DefenseApiRequest ret = new DefenseApiRequest();
                BeanUtils.copyProperties(req, ret);
                ret.setContent(temp);
                return ret;
            }
        }

        return null;
    }

    @Override
    public List<History> latestSliceWithoutCache(String businessName, String sessionId, Integer messageId, int checkNum) throws Exception {
        List<History> result = Lists.newArrayList();
        if (StringUtils.isEmpty(sessionId)) {
            return result;
        }

        if (messageId == null) {
            return result;
        }

        if (checkNum <= 0) {
            return result;
        }

        int fetchNum = checkNum + 3;
        List<String> historyStrs = storageService.lrange(ContentHelper.historyKey(ContentHelper.globalSessionKey(businessName, sessionId)), -fetchNum, -1, String.class);
        historyStrs = Lists.reverse(historyStrs);
        logger.debug("获取会话历史，sessionId={}, messageId={}, histories={}", sessionId, messageId, historyStrs);
        int curNum = 0;
        for (String historyStr : historyStrs) {
            History history = History.fromJson(historyStr);
            HistoryType type = history.getType();

            if (Objects.equals(HistoryType.system, type)) {
                List<DefenseApiResponse> data = history.getData();
                if (CollectionUtils.isNotEmpty(data) && Objects.equals(data.get(0).getRequests().get(0).getMessageId(), messageId)) {
                    result.add(0, history);
                    continue;
                } else {
                    return result;
                }
            }

            List<DefenseApiRequest> data = (List<DefenseApiRequest>)history.getData();
            if (!Objects.equals(messageId, data.get(0).getMessageInfo().getMessageId())) {
                return result;
            }

            if (curNum >= checkNum) {
                return result;
            }

            curNum += history.getData().size();

            result.add(0, history);
        }

        return result;
    }

    @Override
    public List<DefenseApiRequest> latestSliceWithoutCache(String businessName, String sessionId, Integer messageId) throws Exception {
        List<DefenseApiRequest> result = Lists.newArrayList();
        if (StringUtils.isEmpty(businessName) || StringUtils.isEmpty(sessionId)) {
            return result;
        }

        if (messageId == null) {
            return result;
        }

        boolean shoudBreak = false;
        int batch = 30;
        for (int i = 0; i < 3; i++) {
            int start = -(i+1) * batch;
            int end = start + batch - 1;
            List<String> historyStrs = storageService.lrange(ContentHelper.historyKey(ContentHelper.globalSessionKey(businessName, sessionId)), start, end, String.class);
            if (CollectionUtils.isEmpty(historyStrs)) {
                shoudBreak = true;
                break;
            }

            List<History> temp = historyStrs.stream().map(History::fromJson).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(temp)) {
                shoudBreak = true;
            }

            History x = temp.get(0);
            if (Objects.equals(x.getType(), HistoryType.user)) {
                shoudBreak = true;
            } else {
                List<DefenseApiRequest> data = (List<DefenseApiRequest>)x.getData();
                if (!Objects.equals(messageId, data.get(0).getMessageInfo().getMessageId())) {
                    shoudBreak = true;
                }
            }

            List<DefenseApiRequest> resultTemp = Lists.newArrayList();
            temp.forEach(his -> {
                if (Objects.equals(his.getType(), HistoryType.user)) {
                    return;
                }

                List<DefenseApiRequest> data = (List<DefenseApiRequest>)his.getData();
                if (!Objects.equals(messageId, data.get(0).getMessageInfo().getMessageId())) {
                    return;
                }

                resultTemp.addAll(data);
            });

            resultTemp.addAll(result);
            result = resultTemp;

            if (shoudBreak) {
                break;
            }
        }
        if (!shoudBreak) {
            logger.warn("分片超长了，businessName={}, sessionId={}, messageId={}", businessName, sessionId, messageId);
        }
        return result;
    }

    @Override
    public List<History> latestMessage(String businessName, String sessionId, int checkNum, boolean fromUser, SessionContext context) throws Exception {
        if (StringUtils.isNotEmpty(threadLocalService.sessionId.get()) && CollectionUtils.isNotEmpty(threadLocalService.histories.get())) {
            DefenseApiRequest temp = threadLocalService.lastUserReq.get();
            if (temp != null) {
                context.setLastUserReq(temp);
            }
            return threadLocalService.histories.get();
        }

        List<History> result = latestMessageWithoutCache(businessName, sessionId, checkNum, fromUser);
        DefenseApiRequest temp = buildLastUserReq(fromUser, businessName, result);
        if (temp != null) {
            context.setLastUserReq(temp);
        }
        if (StringUtils.isNotEmpty(threadLocalService.sessionId.get())) {
            threadLocalService.histories.set(result);
            if (temp != null) {
                threadLocalService.lastUserReq.set(temp);
            }
        }
        return result;
    }

    @Override
    public List<History> latestMessageWithoutCache(String businessName, String sessionId, int checkNum, boolean fromUser) throws Exception {
        List<History> result = Lists.newArrayList();
        if (StringUtils.isEmpty(sessionId)) {
            return result;
        }

        if (checkNum <= 0) {
            return result;
        }

        List<String> historyStrs = storageService.lrange(ContentHelper.historyKey(ContentHelper.globalSessionKey(businessName, sessionId)), -checkNum, -1, String.class);
        historyStrs = Lists.reverse(historyStrs);
        logger.debug("获取会话历史，sessionId={}, histories={}", sessionId, historyStrs);

        final AtomicInteger curNum = new AtomicInteger(0);
        // 兼容分片传输的情况
        Set<Integer> messageIds = Sets.newHashSet();
        for (String historyStr : historyStrs) {
            if (messageIds.size() >= checkNum || curNum.get() >= checkNum) {
                return result;
            }

            History history = History.fromJson(historyStr);
            HistoryType type = history.getType();

            if (Objects.equals(HistoryType.system, type)) {
                result.add(0, history);
                continue;
            }

            List<DefenseApiRequest> data = (List<DefenseApiRequest>)history.getData();
            data.forEach(x -> {
                Integer messageId = x.getMessageInfo().getMessageId();
                if (messageId != null) {
                    messageIds.add(messageId);
                } else {
                    curNum.incrementAndGet();
                }
            });
            result.add(0, history);
        }

        return result;
    }

    private ExecutorService pool = Executors.newFixedThreadPool(3);
    @Override
    public void addHistory(SessionContext context, String businessName, String sessionId, List<History> histories) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotEmpty(businessName), "businessName不能为空");
        if (StringUtils.isEmpty(sessionId)) {
            return;
        }

        if (CollectionUtils.isEmpty(histories)) {
            return;
        }

        String key = ContentHelper.historyKey(ContentHelper.globalSessionKey(businessName, sessionId));
        if (Objects.equals(ResponseMode.sync, context.getCurReq().get(0).getResponseMode())) {
            pool.submit(() -> doAddHistory(businessName, sessionId, histories, key));
        } else {
            doAddHistory(businessName, sessionId, histories, key);
        }

        if (StringUtils.isNotEmpty(threadLocalService.sessionId.get())) {
            threadLocalService.histories.get().addAll(histories);
            int checkNum = BertUtils.BERT_LEN; // 考虑bert在流式检测中最长是512
            int size = threadLocalService.histories.get().size();
            if (size > checkNum) {
                threadLocalService.histories.set(threadLocalService.histories.get().subList(size - checkNum, size));
            }
        }
    }

    private void doAddHistory(String businessName, String sessionId, List<History> histories, String key) {
        try {
            storageService.execute(jedis -> {
                Pipeline pipelined = jedis.pipelined();
                for (History history : histories) {
                    pipelined.rpush(key, JSON.toJSONString(history));
                }

                pipelined.expire(key, HISTORY_EXPIRE_SECONDS);
                pipelined.sync();
                return null;
            });
        } catch (Exception e) {
            logger.error("写入会话记录失败，business={}, session={}, history={}", businessName, sessionId, JSON.toJSONString(histories));
        }
    }

    @Override
    public void addHistoryV2(String businessName, String sessionId, DefenseApiRequest request, Message message) throws Exception {
        String historyKeyV2 = ContentHelper.historyKeyV2(businessName, sessionId);
        storageService.execute(jedis -> {
            Pipeline pipelined = jedis.pipelined();
            pipelined.rpush(historyKeyV2, JSON.toJSONString(message));
            pipelined.expire(historyKeyV2, HISTORY_EXPIRE_SECONDS * 4);

            pipelined.sync();
            return null;
        });
    }

    @Override
    public List<Message> latestHistoryV2(String businessName, String sessionId, int count) throws Exception {
        String key = ContentHelper.historyKeyV2(businessName, sessionId);
        return storageService.lrange(key, -count, -1, Message.class);
    }
}
