package com.jd.security.llmsec.service.task;

import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.api.defense.DefenseResultFetchRequest;

import java.util.List;



public interface TaskExecutor {
    String SESSION_EXECUTE_RESP_PREFIX = "session:resp:";
    int RESP_EXPIRE_SECONDS = 300;
    String SESSION_EXECUTE_LOCK_PREFIX = "session:lock:";

    void execute(String sessionId);

    List<DefenseApiResponse> fetchResult(String businessName, String sessionId, DefenseResultFetchRequest.Type type);
}
