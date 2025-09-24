package com.jd.security.llmsec.core.check;

import lombok.Data;

import java.util.List;



@Data
public class MultiTurnDetectCheck extends RiskCheckResult{
    @Override
    public RiskCheckType type() {
        return RiskCheckType.multi_turn_detect;
    }

    private String reason;
}
