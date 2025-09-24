package com.jd.security.llmsec.service.execute.functions;

import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.engine.AbstractFunction;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;


public class MultiLablePreFunction extends AbstractFunction {
    public MultiLablePreFunction(FunctionConfig functionConf) throws Exception {
        super(functionConf);
    }

    @Override
    protected void doInit(Map<String, Object> conf) {

    }

    @Override
    public RiskCheckType type() {
        return RiskCheckType.multi_label_pred;
    }

    @Override
    public RiskCheckResult doRun(SessionContext context) throws ExceptionWithCode {
        // todo 后续支持
        return null;
    }
}
