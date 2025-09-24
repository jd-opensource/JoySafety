package com.jd.security.llmsec.core.openai;

import lombok.Data;



@Data
public class Message {
    private Role role;
    private String content;

    public Message() {
    }

    public Message(Role role, String content) {
        this.role = role;
        this.content = content;
    }
}
