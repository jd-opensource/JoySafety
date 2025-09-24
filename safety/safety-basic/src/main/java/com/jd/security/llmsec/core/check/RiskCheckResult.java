package com.jd.security.llmsec.core.check;

import lombok.Data;

import java.util.Objects;



@Data
public abstract class RiskCheckResult {
    abstract public RiskCheckType type();
    private RiskCheckType type;
    private Integer riskCode = -1;
    private String riskMessage;
    private String srcName;
    private String checkedContent;
    private long cost;
    // 兼容最终结果判断在原子能力中的情况
    private Boolean canBeResult = null;

    public RiskCheckResult() {
        this.type = type();
    }

    public boolean hasRisk() {
        return !Objects.equals(0, riskCode);
    }
}
