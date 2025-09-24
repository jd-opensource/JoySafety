package com.jd.security.llmsec.service.storage;

import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.session.SessionContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;



@Service
public class MessageQueueServiceImpl implements MessageQueueService {
    private Logger logger = LoggerFactory.getLogger(MessageQueueServiceImpl.class);

    @PostConstruct
    private void init() {
    }

    @PreDestroy
    private void destroy() {
    }

    //  todo 增加输出到mq
    @Override
    public void appendResponseData(SessionContext context, String accessKey, String sessionId, DefenseApiResponse response) {
    }

    @Override
    public void appendContextData(String accessKey, String sessionId, String contextData) {
    }
}
