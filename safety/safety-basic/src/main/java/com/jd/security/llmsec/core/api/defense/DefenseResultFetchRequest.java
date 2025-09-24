package com.jd.security.llmsec.core.api.defense;

import com.alibaba.fastjson2.JSONObject;
import com.jd.security.llmsec.core.auth.AuthRequest;
import lombok.Data;



@Data
public class DefenseResultFetchRequest extends AuthRequest {
    private String sessionId;
    private Type type = Type.latest;
    private JSONObject ext;

    public enum Type {
        /**
         * 会话对应所有检测结果
         */
        all,

        /**
         * 最新检测结果
         */
        latest
    }
}
