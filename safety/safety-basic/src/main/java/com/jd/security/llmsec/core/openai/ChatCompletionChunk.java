package com.jd.security.llmsec.core.openai;

import lombok.Data;

import java.util.List;



@Data
public class ChatCompletionChunk {
    private String id;
    private String object;
    private long created;
    private String model;
    private String system_fingerprint;
    private List<Choice> choices;

    @Data
    public static class Choice {
        private int index;
        private Message delta;
        private Object logprobs;
        private Object finish_reason;
    }
}
