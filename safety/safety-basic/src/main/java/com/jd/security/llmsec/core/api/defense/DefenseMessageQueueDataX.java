package com.jd.security.llmsec.core.api.defense;

import lombok.Data;



@Data
public class DefenseMessageQueueDataX {
    private String accessKey;
    private String sessionId;
    /**
     * 执行耗时
     */
    private long executeCost;

    /**
     * 从接收到执行完的时间
     */
    private long recvExecuteCost;

    /**
     * 消息发送时的时间
     */
    private long timestamp;
    private DefenseApiResponseX response;

    public DefenseMessageQueueDataX() {
    }

    public DefenseMessageQueueDataX(String accessKey, String sessionId, DefenseApiResponseX response) {
        this.accessKey = accessKey;
        this.sessionId = sessionId;
        this.response = response;
    }
}
