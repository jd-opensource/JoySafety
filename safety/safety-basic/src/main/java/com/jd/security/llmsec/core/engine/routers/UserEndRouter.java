package com.jd.security.llmsec.core.engine.routers;

import com.jd.security.llmsec.core.conf.RouterConfig;
import com.jd.security.llmsec.core.conf.RouterType;
import com.jd.security.llmsec.core.engine.Node;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;

import java.util.Map;



public class UserEndRouter extends SimpleNextRouter {
    public UserEndRouter(RouterConfig routerConf, Map<String, Node> nodeMap) {
        super(routerConf, nodeMap);
    }

    @Override
    public Node next(SessionContext context) throws ExceptionWithCode {
        return context.getCurReq().get(0).fromUser() ? null : super.next(context);
    }

    @Override
    public RouterType type() {
        return RouterType.user_end;
    }
}
