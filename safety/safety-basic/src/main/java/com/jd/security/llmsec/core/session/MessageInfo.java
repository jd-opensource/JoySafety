package com.jd.security.llmsec.core.session;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;


@Data
public class MessageInfo extends MessageBase{
    /**
     * 消息来源的角色：
     * - 用户输入数据可传user
     * - 大模型返回数据可传robot
     */
    private Role fromRole;

    /**
     * - 如果角色是user，可取用户的唯一标识；
     * - 如果角色是robot，可取robot的标识（如果只有一个robot的话，取一个固定的值即可；如果多个robot的话，取可以区分robot的标识）
     */
    private String fromId;

    /**
     * 消息接收者的角色：
     * - 用户接收可传user
     * - 大模型接收可传robot
     */
    private Role toRole;

    /**
     * - 如果角色是user，可取用户的唯一标识；
     * - 如果角色是robot，可取固定标识`robot`
     */
    private String toId;

    /**
     * 扩展字符
     */
    private JSONObject ext;


    public static MessageInfoBuilder messageInfoBuilder() {
        return new MessageInfoBuilder();
    }

    public static class MessageInfoBuilder {
        private MessageInfo messageInfo;

        private MessageInfoBuilder() {
            this.messageInfo = new MessageInfo();
        }

        public MessageInfoBuilder sessionId(String sessionId) {
            this.messageInfo.setSessionId(sessionId);
            return this;
        }

        public MessageInfoBuilder messageId(Integer messageId) {
            this.messageInfo.setMessageId(messageId);
            return this;
        }

        public MessageInfoBuilder sliceId(Integer sliceId) {
            this.messageInfo.setSliceId(sliceId);
            return this;
        }

        public MessageInfoBuilder fromRole(Role fromRole) {
            this.messageInfo.setFromRole(fromRole);
            return this;
        }

        public MessageInfoBuilder fromId(String fromId) {
            this.messageInfo.setFromId(fromId);
            return this;
        }

        public MessageInfoBuilder toRole(Role toRole) {
            this.messageInfo.setToRole(toRole);
            return this;
        }

        public MessageInfoBuilder toId(String toId) {
            this.messageInfo.setToId(toId);
            return this;
        }

        public MessageInfoBuilder ext(JSONObject ext) {
            this.messageInfo.setExt(ext);
            return this;
        }

        public MessageInfo build() {
            return this.messageInfo;
        }
    }

}
