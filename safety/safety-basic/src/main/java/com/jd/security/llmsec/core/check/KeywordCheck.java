package com.jd.security.llmsec.core.check;

import lombok.Data;



@Data
public class KeywordCheck extends RiskCheckResult{
    @Override
    public RiskCheckType type() {
        return RiskCheckType.keyword;
    }

    /**
     * riskCode取0时，riskMessage取`无风险`
     * riskCode非0时，riskMessage取对应风险类别
     * 支持的风险类别参 {@link RiskInfo}
     */
//    private Integer riskCode;
//    private String riskMessage;

    /**
     * 命中词
     */
    private String hitWord;

    /**
     * 命中词的场景编号
     */
    private Integer firstClassNo;

    /**
     * 命中词的场景编号
     */
    private String firstClassName;

    /**
     * 命中词的类型
     * 1.黑 2:白 3:灰
     */
    private Integer bwgLabel;

    private Integer srcNo;
    private Integer latency;

    public enum Label {
        /**
         * 未命中
         */
        nohit(0),

        /**
         * 命中黑名单
         */
        black(1),

        /**
         * 命中白名单
         */
        white(2),

        /**
         * 命中灰名单
         */
        grey(3);

        private final Integer code;
        Label(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public static KeywordCheckBuilder keywordCheckBuilder() {
        return new KeywordCheckBuilder();
    }

    public static class KeywordCheckBuilder {
        private KeywordCheck keywordCheck;

        private KeywordCheckBuilder() {
            this.keywordCheck = new KeywordCheck();
        }

        public KeywordCheckBuilder riskCode(Integer riskCode) {
            this.keywordCheck.setRiskCode(riskCode);
            return this;
        }

        public KeywordCheckBuilder riskMessage(String riskMessage) {
            this.keywordCheck.setRiskMessage(riskMessage);
            return this;
        }

        public KeywordCheckBuilder hitWord(String hitWord) {
            this.keywordCheck.setHitWord(hitWord);
            return this;
        }

        public KeywordCheckBuilder firstClassNo(Integer firstClassNo) {
            this.keywordCheck.setFirstClassNo(firstClassNo);
            return this;
        }

        public KeywordCheckBuilder firstClassName(String firstClassName) {
            this.keywordCheck.setFirstClassName(firstClassName);
            return this;
        }

        public KeywordCheckBuilder bwgLabel(Integer bwgLabel) {
            this.keywordCheck.setBwgLabel(bwgLabel);
            return this;
        }

        public KeywordCheckBuilder srcNo(Integer srcNo) {
            this.keywordCheck.setSrcNo(srcNo);
            return this;
        }

        public KeywordCheckBuilder latency(Integer latency) {
            this.keywordCheck.setLatency(latency);
            return this;
        }

        public KeywordCheck build() {
            return this.keywordCheck;
        }
    }
}
