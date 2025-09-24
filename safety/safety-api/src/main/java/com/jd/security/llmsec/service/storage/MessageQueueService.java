package com.jd.security.llmsec.service.storage;

import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.session.SessionContext;



public interface MessageQueueService {
    void appendResponseData(SessionContext context, String accessKey, String sessionId, DefenseApiResponse response);
    void appendContextData(String accessKey, String sessionId, String contextData);
}
