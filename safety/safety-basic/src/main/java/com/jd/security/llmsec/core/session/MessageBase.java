package com.jd.security.llmsec.core.session;

import lombok.Data;



@Data
public class MessageBase {
    /**
     * 会话id
     */
    private String sessionId;
    /**
     * 消息id
     */
    private Integer messageId;

    /**
     * 消息分片id(针对消息过长，流式输出的情况)
     */
    private Integer sliceId;

    public static MessageBaseBuilder messageBaseBuilder() {
        return new MessageBaseBuilder();
    }

    public static class MessageBaseBuilder {
        private MessageBase messageBase;

        private MessageBaseBuilder() {
            this.messageBase = new MessageBase();
        }

        public MessageBaseBuilder sessionId(String sessionId) {
            this.messageBase.setSessionId(sessionId);
            return this;
        }

        public MessageBaseBuilder messageId(Integer messageId) {
            this.messageBase.setMessageId(messageId);
            return this;
        }

        public MessageBaseBuilder sliceId(Integer sliceId) {
            this.messageBase.setSliceId(sliceId);
            return this;
        }

        public MessageBase build() {
            return this.messageBase;
        }
    }
}
