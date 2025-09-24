package com.jd.security.llmsec.core.engine.routers;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Preconditions;
import com.jd.security.llmsec.core.conf.RouterConfig;
import com.jd.security.llmsec.core.conf.RouterType;
import com.jd.security.llmsec.core.engine.Node;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;
import lombok.Data;

import java.util.Map;



public class SimpleNextRouter extends AbstractRouter {
    public SimpleNextRouter(RouterConfig routerConf, Map<String, Node> nodeMap) {
        super(routerConf, nodeMap);
    }

    private Node next;
    @Override
    public Node next(SessionContext context) throws ExceptionWithCode {
        return next;
    }

    @Override
    public RouterType type() {
        return RouterType.simple_next;
    }

    @Data
    public static class Conf {
        private String nodeId;
    }

    @Override
    protected void doInit(Map<String, Object> conf, Map<String, Node> nodeMap) {
        Conf routeConf = JSON.parseObject(JSON.toJSONString(conf), Conf.class);
        Preconditions.checkArgument(routeConf.getNodeId() != null, "nodeId为空");
        Node node = nodeMap.get(routeConf.getNodeId());
        Preconditions.checkArgument(node != null, "node不存在");
        this.next = node;
    }
}
