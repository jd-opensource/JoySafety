package com.jd.security.llmsec.core.session;

import lombok.Data;



@Data
public class CheckConf {
    private CheckUnit unit;
    private int checkNum = 1;
    private Long timeoutMilliseconds = 1000l;
    private Long adaptivePauseMilliseconds = 50l;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CheckConf checkConf = new CheckConf();

        private Builder() {
        }

        public Builder unit(CheckUnit unit) {
            checkConf.unit = unit;
            return this;
        }

        public Builder checkNum(int checkNum) {
            checkConf.checkNum = checkNum;
            return this;
        }

        public CheckConf build() {
            return checkConf;
        }
    }
}
