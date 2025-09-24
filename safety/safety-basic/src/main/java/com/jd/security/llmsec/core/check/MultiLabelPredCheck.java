package com.jd.security.llmsec.core.check;

import lombok.Data;

import java.util.List;



@Data
public class MultiLabelPredCheck extends RiskCheckResult{
    @Override
    public RiskCheckType type() {
        return RiskCheckType.multi_label_pred;
    }

    private RiskInfo maxRisk;
    private List<RiskInfo> risks;
}
