package com.jd.security.llmsec.core.check;

import lombok.Data;


@Data
public class AsyncCheckResult extends RiskCheckResult{
    @Override
    public RiskCheckType type() {
        return RiskCheckType.async;
    }
    private RiskCheckResult result;
}
