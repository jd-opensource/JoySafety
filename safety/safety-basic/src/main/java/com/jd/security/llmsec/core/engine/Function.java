package com.jd.security.llmsec.core.engine;

import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;



public interface Function {
    FunctionConfig conf();

    /**
     * 获取风险检查类型
     * @return 风险检查类型
     */
    RiskCheckType type();

    /**
     * 返回名称
     * @return 名称字符串
     */
    String name();

    RiskCheckResult run(SessionContext context) throws ExceptionWithCode;
}
