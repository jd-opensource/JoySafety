package com.jd.security.llmsec.service.execute.functions;

import com.jd.security.llmsec.core.check.MixedCheck;
import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.engine.AbstractFunction;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;

import java.util.Map;



public class DummyFunction extends AbstractFunction {
    public DummyFunction(FunctionConfig functionConf) throws Exception {
        super(functionConf);
    }

    @Override
    protected void doInit(Map<String, Object> conf) throws Exception {}

    @Override
    protected RiskCheckResult doRun(SessionContext context) throws ExceptionWithCode {
        return MixedCheck.noRisk();
    }

    @Override
    public RiskCheckType type() {
        return RiskCheckType.dummy;
    }
}
