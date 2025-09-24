package com.jd.security.llmsec.core.engine;

import com.jd.security.llmsec.core.conf.RouterConfig;
import com.jd.security.llmsec.core.conf.RouterType;
import com.jd.security.llmsec.core.engine.routers.*;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;

import java.util.HashMap;
import java.util.Map;



public interface Router {
    RouterConfig conf();
    Node next(SessionContext context) throws ExceptionWithCode;
    RouterType type();
    String name();

    Map<RouterType, Class<? extends AbstractRouter>> ROUTER_MAP = new HashMap<RouterType, Class<? extends AbstractRouter>>() {{
        put(RouterType.groovy, GroovyRouter.class);
        put(RouterType.keyword, KeywordRouter.class);
        put(RouterType.user_end, UserEndRouter.class);
        put(RouterType.robot_end, RobotEndRouter.class);
        put(RouterType.stupid_end, StupidEnd.class);
        put(RouterType.simple_next, SimpleNextRouter.class);
        put(RouterType.risk_end, RiskEnd.class);
    }};

}
