package com.jd.security.llmsec.core.conf.funcs;

import com.jd.security.llmsec.core.openai.Message;
import lombok.Data;

import java.util.List;



@Data
public class MultiTurnDetectConf {
    private String url;
    // 包括各个角色的对大轮数
    private Integer maxTurns = 5;

    @Data
    public static class Req {
        private String request_id;
        private String business_id;
        private String session_id;
        private List<Message> history;
    }
}
