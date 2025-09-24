package com.jd.security.llmsec.core.auth;

import com.google.common.base.Preconditions;
import com.jd.security.llmsec.core.RequestBase;
import com.jd.security.llmsec.core.ResponseCode;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.util.SignUtil;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;



@Data
public class AuthRequest extends RequestBase {
    private String accessKey;
    private String accessTarget;
    private String plainText;
    private String signature;

    public void basicCheck(AuthControlConf controlConf) throws ExceptionWithCode {
        if (controlConf == null) {
            throw new ExceptionWithCode(ResponseCode.not_allowed.code, "未授权");
        }

        if (accessTarget == null) {
            accessTarget = "";
        }

        if (CollectionUtils.isNotEmpty(controlConf.getAccessTargets()) && StringUtils.isNotEmpty(accessTarget)) {
            if (!controlConf.getAccessTargets().contains(accessTarget)) {
                throw new ExceptionWithCode(ResponseCode.not_allowed.code, "接口未授权");
            }
        }
        try {
            Preconditions.checkArgument(this.getTimestamp() > 0, "timestamp需要大于0");
            Preconditions.checkArgument(StringUtils.isNotEmpty(getRequestId()), "requestId不能为空");
            Preconditions.checkArgument(StringUtils.isNotEmpty(getAccessKey()), "accessKey不能为空");
            Preconditions.checkArgument(StringUtils.isNotEmpty(getPlainText()), "plainText不能为空");
            Preconditions.checkArgument(StringUtils.isNotEmpty(getSignature()), "signature不能为空");
        } catch (Exception e) {
            throw new ExceptionWithCode(ResponseCode.param_invalid.code, e.getMessage());
        }

        if (System.currentTimeMillis() - this.getTimestamp() > controlConf.getExpireSeconds() * 1000) {
            throw new ExceptionWithCode(ResponseCode.sign_expire.code, ResponseCode.sign_expire.description);
        }

        boolean verify = SignUtil.verify(controlConf, this);
        if (!verify) {
            throw new ExceptionWithCode(ResponseCode.sign_not_matched.code, ResponseCode.sign_not_matched.description);
        }
    }
}
