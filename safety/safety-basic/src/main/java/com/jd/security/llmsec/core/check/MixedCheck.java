package com.jd.security.llmsec.core.check;

import lombok.Data;

import java.util.Map;



@Data
public class MixedCheck extends RiskCheckResult{
    @Override
    public RiskCheckType type() {
        return RiskCheckType.mixed;
    }

    private Map<String,Object> extra;
    public static MixedCheck noRisk() {
        return noRisk("none");
    }

    public static MixedCheck noRisk(String srcName) {
        MixedCheck check = new MixedCheck();
        check.setRiskCode(0);
        check.setSrcName(srcName);
        check.setRiskMessage("正常文本");
        return check;
    }
}
