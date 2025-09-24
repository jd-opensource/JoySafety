package com.jd.security.llmsec.core.engine;

import com.google.common.base.Preconditions;
import com.jd.security.llmsec.core.check.ParallelCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;



public abstract class AbstractFunction implements Function{
    private final FunctionConfig functionConf;
    public AbstractFunction(FunctionConfig functionConf) throws Exception {
        this.functionConf = functionConf;

        Preconditions.checkNotNull(functionConf, "functionConf不能为空");
        Preconditions.checkNotNull(functionConf.getType(), "functionConf.type不能为空");
        Preconditions.checkNotNull(functionConf.getTimeoutMilliseconds(), "functionConf.timeoutMilliseconds不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(functionConf.getName()), "functionConf.name不能为空");

        doInit(functionConf.getConf());
    }

    @Override
    public FunctionConfig conf() {
        return this.functionConf;
    }

    protected abstract void doInit(Map<String, Object> conf) throws Exception;

    @Override
    public RiskCheckResult run(SessionContext context) throws ExceptionWithCode {
        long start = System.currentTimeMillis();
        RiskCheckResult result = doRun(context);

        if (Objects.equals(this.type(), RiskCheckType.parallel)) {
            ParallelCheckResult tmp = new ParallelCheckResult();
            tmp.setSrcName(name());
            tmp.setCost(System.currentTimeMillis() - start);
            context.appendResult(tmp);
        } else {
            if (result != null) {
                result.setSrcName(name());
                context.appendResult(result);
                result.setCost(System.currentTimeMillis() - start);
            }
        }

        return result;
    }

    protected abstract RiskCheckResult doRun(SessionContext context) throws ExceptionWithCode ;

    @Override
    public String name() {
        return this.functionConf.getName();
    }
}
