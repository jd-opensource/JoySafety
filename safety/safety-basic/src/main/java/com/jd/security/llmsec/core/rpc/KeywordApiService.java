package com.jd.security.llmsec.core.rpc;

import lombok.Data;

import java.util.Objects;



public interface KeywordApiService {
    @Data
    class KVRequest {
        private String content;
        private String businessName;
        private String requestId;
    }

    @Data
    class KVResponse {
        private Integer statusCode;
        private String hitWord;
        private Integer firstClassNo;
        private String firstClassName;
        private Integer secondClassNo;
        private String secondClassName;
        private Integer bwgLabel;
        private long latency;

        //
        public boolean success() {
            return Objects.equals(200, statusCode);
        }

        public KVResponse() {
        }

        public KVResponse(Integer statusCode, String hitWord, Integer firstClassNo, String firstClassName, Integer secondClassNo, String secondClassName, Integer bwgLabel, long latency) {
            this.statusCode = statusCode;
            this.hitWord = hitWord;
            this.firstClassNo = firstClassNo;
            this.firstClassName = firstClassName;
            this.secondClassNo = secondClassNo;
            this.secondClassName = secondClassName;
            this.bwgLabel = bwgLabel;
            this.latency = latency;
        }
    }

    KVResponse invoke(KVRequest request);
}
