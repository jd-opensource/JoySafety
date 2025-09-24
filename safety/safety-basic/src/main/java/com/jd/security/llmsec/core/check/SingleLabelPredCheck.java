package com.jd.security.llmsec.core.check;

import lombok.Data;

import java.util.List;



@Data
public class SingleLabelPredCheck extends RiskCheckResult{
    @Override
    public RiskCheckType type() {
        return RiskCheckType.single_label_pred;
    }

    /**
     * 预测概率
     */
    private float probability;

    /**
     * 仅调试使用
     */
    private List<RiskInfo> detail;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SingleLabelPredCheck singleLabelPredCheck;

        private Builder() {
            this.singleLabelPredCheck = new SingleLabelPredCheck();
        }

        public Builder riskCode(Integer riskCode) {
            this.singleLabelPredCheck.setRiskCode(riskCode);
            return this;
        }

        public Builder riskMessage(String riskMessage) {
            this.singleLabelPredCheck.setRiskMessage(riskMessage);
            return this;
        }

        public Builder probability(float probability) {
            this.singleLabelPredCheck.probability = probability;
            return this;
        }

        public SingleLabelPredCheck build() {
            return this.singleLabelPredCheck;
        }
    }

    @Data
    public static class SkillsReq {
        private String request_id;
        private String business_id;
        private String session_id;
        private List<String> text_list;
    }

    @Data
    public static class SkillsResp extends RiskInfo{
        private String text;
        private List<RiskInfo> detail;
    }
}
