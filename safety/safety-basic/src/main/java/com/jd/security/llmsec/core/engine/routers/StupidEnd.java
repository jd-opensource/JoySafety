package com.jd.security.llmsec.core.engine.routers;

import com.jd.security.llmsec.core.conf.RouterConfig;
import com.jd.security.llmsec.core.conf.RouterType;
import com.jd.security.llmsec.core.engine.Node;
import com.jd.security.llmsec.core.session.SessionContext;

import java.util.Map;



public class StupidEnd extends AbstractRouter {
    public StupidEnd(RouterConfig routerConf, Map<String, Node> nodeMap) {
        super(routerConf, nodeMap);
    }

    @Override
    protected void doInit(Map<String, Object> conf, Map<String, Node> nodeMap) {}

    @Override
    public Node next(SessionContext context) {
        return null;
    }

    @Override
    public RouterType type() {
        return RouterType.stupid_end;
    }
}
