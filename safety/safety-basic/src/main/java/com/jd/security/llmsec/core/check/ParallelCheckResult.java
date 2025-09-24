package com.jd.security.llmsec.core.check;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class ParallelCheckResult extends RiskCheckResult{
    @Override
    public RiskCheckType type() {
        return RiskCheckType.parallel;
    }
    private Map<String, RiskCheckResult> resultMap;
}
