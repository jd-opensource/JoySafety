package com.jd.security.llmsec.core.api.defense;

import com.jd.security.llmsec.core.auth.AuthControlConf;
import com.jd.security.llmsec.core.auth.AuthRequest;
import com.jd.security.llmsec.core.openai.Message;
import com.jd.security.llmsec.core.session.*;
import com.jd.security.llmsec.core.util.SignUtil;
import lombok.Data;

import java.util.List;
import java.util.Objects;



@Data
public class DefenseApiRequest extends AuthRequest {
    private MessageInfo messageInfo;

    private ContentType contentType = ContentType.text;

    /**
     * 消息体
     */
    private String content;

    /**
     * 用于兼容openai格式的大模型输入
     */
    private transient List<Message> openaiMessages;

    /**
     * 业务的类型
     */
    private BusinessType businessType;

    /**
     * 数据返回模式
     */
    private ResponseMode responseMode;

    /**
     * 用于测试/实验
     */
    private AB ab;

    public boolean fromRobot() {
        return Objects.equals(Role.robot, this.messageInfo.getFromRole());
    }

    public boolean fromUser() {
        return Objects.equals(Role.user, this.messageInfo.getFromRole());
    }

    public void sign(AuthControlConf authControlConf) {
        this.setAccessKey(authControlConf.getAccessKey());
        this.setPlainText(this.getContent());
        this.setSignature(SignUtil.sign(authControlConf, this));
    }

    public static Builder  builder() {
        return new Builder();
    }

    public static class Builder {
        private DefenseApiRequest request;

        private Builder() {
            this.request = new DefenseApiRequest();
        }

        public Builder timestamp(long timestamp) {
            this.request.setTimestamp(timestamp);
            return this;
        }

        public Builder requestId(String requestId) {
            this.request.setRequestId(requestId);
            return this;
        }

        public Builder accessKey(String accessKey) {
            this.request.setAccessKey(accessKey);
            return this;
        }

        public Builder accessTarget(String accessTarget) {
            this.request.setAccessTarget(accessTarget);
            return this;
        }

        public Builder plainText(String plainText) {
            this.request.setPlainText(plainText);
            return this;
        }

        public Builder signature(String signature) {
            this.request.setSignature(signature);
            return this;
        }

        public Builder messageInfo(MessageInfo messageInfo) {
            this.request.setMessageInfo(messageInfo);
            return this;
        }

        public Builder contentType(ContentType contentType) {
            this.request.setContentType(contentType);
            return this;
        }

        public Builder content(String content) {
            this.request.setContent(content);
            return this;
        }

        public Builder businessType(BusinessType businessType) {
            this.request.setBusinessType(businessType);
            return this;
        }

        public Builder responseMode(ResponseMode responseMode) {
            this.request.setResponseMode(responseMode);
            return this;
        }

        public DefenseApiRequest build() {
            return this.request;
        }
    }
}
