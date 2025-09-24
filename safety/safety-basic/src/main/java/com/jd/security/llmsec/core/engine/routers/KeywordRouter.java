package com.jd.security.llmsec.core.engine.routers;

import com.jd.security.llmsec.core.ResponseCode;
import com.jd.security.llmsec.core.check.KeywordCheck;
import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.RouterConfig;
import com.jd.security.llmsec.core.conf.RouterType;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;
import com.jd.security.llmsec.core.engine.Node;

import java.util.Map;
import java.util.Objects;




public class KeywordRouter extends SimpleNextRouter {
    public KeywordRouter(RouterConfig routerConf, Map<String, Node> nodeMap) {
        super(routerConf, nodeMap);
    }

    @Override
    public Node next(SessionContext context) throws ExceptionWithCode {
        RiskCheckResult curResult = context.getCurResult();
        if (curResult == null) {
            // 兼容keyword function失败的情况
            return super.next(context);
        }

        if (!Objects.equals(curResult.type(), RiskCheckType.keyword)) {
            throw new ExceptionWithCode(ResponseCode.inner_error.code, "keyword router without keyword function");
        }

        KeywordCheck keywordCheck = (KeywordCheck) curResult;
        if (Objects.equals(KeywordCheck.Label.white.getCode(), keywordCheck.getBwgLabel()) || Objects.equals(KeywordCheck.Label.black.getCode(), keywordCheck.getBwgLabel())) {
                return null;
        }
        return super.next(context);
    }

    @Override
    public RouterType type() {
        return RouterType.keyword;
    }
}
