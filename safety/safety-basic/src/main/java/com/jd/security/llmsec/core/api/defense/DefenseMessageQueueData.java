package com.jd.security.llmsec.core.api.defense;

import lombok.Data;



@Data
public class DefenseMessageQueueData {
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
    private DefenseApiResponse response;

    public DefenseMessageQueueData() {
    }

    public DefenseMessageQueueData(String accessKey, String sessionId, DefenseApiResponse response) {
        this.accessKey = accessKey;
        this.sessionId = sessionId;
        this.response = response;

        this.timestamp = System.currentTimeMillis();
    }
}
