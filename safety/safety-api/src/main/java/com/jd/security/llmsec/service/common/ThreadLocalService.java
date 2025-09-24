package com.jd.security.llmsec.service.common;

import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.session.History;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class ThreadLocalService {
    public ThreadLocal<String> sessionId = new ThreadLocal<>();
    public ThreadLocal<List<History>> histories = new ThreadLocal<>();
    public ThreadLocal<DefenseApiRequest> lastUserReq = new ThreadLocal<>();
}
