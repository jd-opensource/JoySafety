package com.jd.security.llmsec.service;

import com.jd.security.llmsec.config.GlobalConf;
import com.jd.security.llmsec.service.session.SessionService;
import com.jd.security.llmsec.service.storage.MessageQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;



@Service
public class BeanBridge {
    @Autowired
    private SessionService sessionService;
    public volatile static SessionService gSessionService;

    @Autowired
    private GlobalConf globalConf;
    public volatile static GlobalConf gGlobalConf;

    @Autowired
    private MessageQueueService messageQueueService;
    public volatile static  MessageQueueService gMessageQueueService;


    @PostConstruct
    private void init() {
        gSessionService = sessionService;
        gGlobalConf = globalConf;
        gMessageQueueService = messageQueueService;
    }
}
