package com.jd.security.llmsec.core.engine.routers;

import com.google.common.base.Preconditions;
import com.jd.security.llmsec.core.conf.RouterConfig;
import com.jd.security.llmsec.core.engine.Node;
import com.jd.security.llmsec.core.engine.Router;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;



public abstract class AbstractRouter implements Router {
    private final RouterConfig routerConf;
    private final Map<String, Node> nodeMap;
    public AbstractRouter(RouterConfig routerConf, Map<String, Node> nodeMap) {
        Preconditions.checkNotNull(routerConf);
        Preconditions.checkArgument(routerConf.getType() != null, "type不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(routerConf.getName()), "name不能为空");
        Preconditions.checkNotNull(nodeMap);
        this.routerConf = routerConf;
        this.nodeMap = nodeMap;
        doInit(routerConf.getConf(), nodeMap);
    }

    protected abstract void doInit(Map<String, Object> conf, Map<String, Node> nodeMap);

    @Override
    public RouterConfig conf() {
        return routerConf;
    }

    @Override
    public String name() {
        return routerConf.getName();
    }
}
