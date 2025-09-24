package com.jd.security.llmsec.core.check;

import lombok.Data;



@Data
public class RiskInfo {
    private Integer riskCode;
    private String riskMessage;

    private float probability;


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private RiskInfo RiskInfo;

        private Builder() {
            this.RiskInfo = new RiskInfo();
        }

        public RiskInfo.Builder riskCode(Integer riskCode) {
            this.RiskInfo.riskCode = riskCode;
            return this;
        }

        public RiskInfo.Builder riskMessage(String riskMessage) {
            this.RiskInfo.riskMessage = riskMessage;
            return this;
        }

        public RiskInfo.Builder probability(float probability) {
            this.RiskInfo.probability = probability;
            return this;
        }

        public RiskInfo build() {
            return this.RiskInfo;
        }
    }
}
