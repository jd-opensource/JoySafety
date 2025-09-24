package com.jd.security.llmsec.service;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jd.security.llmsec.config.GlobalConf;
import com.jd.security.llmsec.core.BusinessConf;
import com.jd.security.llmsec.core.ResponseCode;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.api.defense.DefenseResultFetchRequest;
import com.jd.security.llmsec.core.engine.FunctionExecutor;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.openai.Message;
import com.jd.security.llmsec.core.session.CheckConf;
import com.jd.security.llmsec.core.session.MessageInfo;
import com.jd.security.llmsec.core.session.ResponseMode;
import com.jd.security.llmsec.core.session.Role;
import com.jd.security.llmsec.service.session.SessionService;
import com.jd.security.llmsec.service.task.TaskService;
import com.jd.security.llmsec.utils.ContextHelper;
import com.jd.security.llmsec.utils.MessageHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



@Service
public class DefenseServiceImpl implements DefenseService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("DynamicConfigService")
    private ConfigService configService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FunctionExecutor functionExecutor;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private GlobalConf globalConf;

    @Override
    public void checkParams(DefenseApiRequest request) throws Exception {
        Preconditions.checkArgument(StringUtils.isNotEmpty(request.getContent()), "content不能为空");
        Preconditions.checkNotNull(request.getResponseMode(), "responseMode不能为空");
        Preconditions.checkNotNull(request.getMessageInfo(), "messageInfo不能为空");

        MessageInfo messageInfo = request.getMessageInfo();
        Preconditions.checkNotNull(messageInfo, "messageInfo不能为空");
        Preconditions.checkNotNull(messageInfo.getFromRole(), "fromRole不能为空");

        if (!Objects.equals(ResponseMode.sync, request.getResponseMode())) {
            Preconditions.checkArgument(StringUtils.isNotEmpty(messageInfo.getSessionId()), "responseMode为非sync时，sessionId不能为空");
        }

        if (messageInfo.getSliceId() !=null) {
            Preconditions.checkArgument(messageInfo.getMessageId() != null, "sliceId不为空时，messageId也不能为空");
        }
    }

    @Autowired
    private RateLimitService rateLimitService;
    @Override
    public boolean rateLimited(DefenseApiRequest request) throws ExceptionWithCode {
        // 先用子配置，没有子配置时用主配置
        BusinessConf businessConf = configService.businessConf(request);
        if (businessConf == null) {
            throw new ExceptionWithCode(ResponseCode.token_error.code, ResponseCode.token_error.description);
        }
        return rateLimitService.hasLimit(businessConf.getAccessKey(), request.getAccessTarget());
    }

    @Override
    public void verify(DefenseApiRequest request) throws ExceptionWithCode {
        if (StringUtils.isEmpty(request.getPlainText())) {
            request.setPlainText(request.getContent());
        }
        BusinessConf businessConf = configService.businessConfByAccessKey(request.getAccessKey());
        request.basicCheck(businessConf);
    }

    @Override
    public void verify(DefenseResultFetchRequest request) throws Exception {
        BusinessConf businessConf = configService.businessConfByAccessKey(request.getAccessKey());
        request.basicCheck(businessConf);
    }

    @Override
    public void preProcess(DefenseApiRequest request) throws ExceptionWithCode {
        if (globalConf.streamBusiness().contains(request.getAccessKey()) && Objects.equals(request.getMessageInfo().getFromRole(), Role.robot)) {
            request.setContent(MessageHelper.parseOpenAIResp(request.getContent()));
            request.setPlainText(request.getContent());
        }

        if (globalConf.openaiRequestBizs().contains(request.getAccessKey()) && Objects.equals(request.getMessageInfo().getFromRole(), Role.user)) {
            List<Message> messages = MessageHelper.parseOpenAIReqMessage(request.getContent());
            if (CollectionUtils.isNotEmpty(messages)) {
                request.setOpenaiMessages(messages);
            }
        }

        if (StringUtils.isNotEmpty(request.getContent()) && request.getContent().length() > GlobalConf.MAX_CHECK_LEN) {
            logger.warn("content超长截断至{}，原请求: {}", GlobalConf.MAX_CHECK_LEN, JSON.toJSONString(request));
            request.setContent(request.getContent().substring(0, GlobalConf.MAX_CHECK_LEN));
        }

        if (StringUtils.isNotEmpty(request.getPlainText()) && request.getPlainText().length() > GlobalConf.MAX_CHECK_LEN * 2) {
            logger.warn("plainText超长截断至{}，原请求: {}", GlobalConf.MAX_CHECK_LEN * 2, JSON.toJSONString(request));
            request.setPlainText(request.getPlainText().substring(0, GlobalConf.MAX_CHECK_LEN * 2));
        }
    }

    @Override
    public void submit(DefenseApiRequest request) {
        try {
            taskService.addTask(request);
        } catch (Exception e) {
            logger.error("提交任务异常", e);
        }
    }
    @Override
    public List<DefenseApiResponse> process(DefenseApiRequest request) throws ExceptionWithCode {
        List<DefenseApiResponse> results = Lists.newArrayList();
        DefenseApiResponse response = functionExecutor.invoke(request);
        if (response == null) {
            return results;
        } else {
            ResponseMode responseMode = request.getResponseMode();
            List<DefenseApiResponse> preResults = Lists.newArrayList();
            switch (responseMode) {
                case sync:
                    MessageInfo messageInfo = request.getMessageInfo();
                    BusinessConf businessConf = configService.businessConf(request);
                    CheckConf checkConf = request.fromRobot() ? businessConf.getRobotCheckConf() : businessConf.getUserCheckConf();
                    /*
                     todo: 1) 需要注意的是：checkNum > 1时一般都会伴随着free_taxi(也就是异步处理)，这个时候需要获取历史识别内容；
                           2) 出现其它case时可以修改以下条件
                     */
                    if (checkConf != null && checkConf.getCheckNum() > 1 && StringUtils.isNotEmpty(messageInfo.getSessionId()) && messageInfo.getMessageId() != null) {
                        preResults = this.fetchResult(request.getAccessKey(), request.getMessageInfo().getSessionId(), DefenseResultFetchRequest.Type.all);
                        results.addAll(preResults);
                    }
                    break;
                case free_taxi:
                    preResults = this.fetchResult(request.getAccessKey(), request.getMessageInfo().getSessionId(), DefenseResultFetchRequest.Type.all);
                    if (CollectionUtils.isNotEmpty(preResults) &&  request.getMessageInfo().getSliceId() != null) {
                        preResults = preResults.stream().filter(x ->
                                Objects.equals(request.getMessageInfo().getMessageId(), x.getRequests().get(0).getMessageId())
                                )
                                .collect(Collectors.toList());
                    }
                    results.addAll(preResults);
                case mq:
                case http:
                    break;
            }

            List<DefenseApiResponse> ret = ContextHelper.filter(request.getMessageInfo().getFromRole(), results);
            ret.add(response);
            return ret;
        }
    }


    @Override
    public List<DefenseApiResponse> fetchResult(DefenseResultFetchRequest request) throws ExceptionWithCode {
        return this.fetchResult(request.getAccessKey(), request.getSessionId(), request.getType());
    }

    @Override
    public List<DefenseApiResponse> fetchResult(String businessName, String sessionId, DefenseResultFetchRequest.Type type) {
        if (StringUtils.isEmpty(sessionId)) {
            return Lists.newArrayList();
        }
        return taskService.fetchResult(businessName, sessionId, type);
    }
}
