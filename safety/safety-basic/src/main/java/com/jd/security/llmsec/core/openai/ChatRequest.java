package com.jd.security.llmsec.core.openai;

import lombok.Data;

import java.util.List;


@Data
public class ChatRequest {
    // https://platform.openai.com/docs/api-reference/chat/create
    private String model;
    private List<Message> messages;
    private Boolean store;
    private Object metadata;
    private Double frequency_penalty;

    private Object logit_bias;
    private Boolean logprobs;
    private Integer top_logprobs;
    private Integer max_tokens;
    private Integer max_completion_tokens;
    private Integer n;
    private List<String> modalities;
    private Object prediction;
    private Double presence_penalty;
    private Integer seed;
    private String stop;
    private Boolean stream;
    private Double temperature;
    private Integer top_p;

    private String user;
}
