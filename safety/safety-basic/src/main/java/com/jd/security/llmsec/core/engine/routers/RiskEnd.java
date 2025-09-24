package com.jd.security.llmsec.core.engine.routers;

import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.conf.RouterConfig;
import com.jd.security.llmsec.core.conf.RouterType;
import com.jd.security.llmsec.core.engine.Node;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;

import java.util.Map;



public class RiskEnd extends SimpleNextRouter {
    public RiskEnd(RouterConfig routerConf, Map<String, Node> nodeMap) {
        super(routerConf, nodeMap);
    }
    @Override
    public Node next(SessionContext context) throws ExceptionWithCode {
        RiskCheckResult curResult = context.getCurResult();
        // 兼容当前结点失败的情况
        if (curResult == null) {
            return super.next(context);
        } else {
            return curResult.hasRisk() ? null : super.next(context);
        }
    }

    @Override
    public RouterType type() {
        return RouterType.risk_end;
    }
}
