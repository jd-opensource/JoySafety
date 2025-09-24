package com.jd.security.llmsec.core.conf.funcs;

import lombok.Data;



@Data
public class RagAnswerConf extends KbSearchConf{
    @Data
    public static class Req extends KbSearchConf.Req{
        private Double threshold = 0.8;
    }

    @Data
    public static class RagResp {
        private String answer;
        private KbSearchConf.Resp refDocs;
    }
}
