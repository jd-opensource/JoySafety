package com.jd.security.llmsec.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.session.MessageInfo;
import com.jd.security.llmsec.core.session.Role;
import com.jd.security.llmsec.core.session.SessionContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



public class ContextHelper {
    private static Logger logger = LoggerFactory.getLogger(ContextHelper.class);
    public static @NotNull DefenseApiResponse buildResp(List<DefenseApiRequest> reqs, RiskCheckResult curResult, SessionContext context) {
        List<MessageInfo> messageInfos = convert(reqs);
        DefenseApiResponse response = DefenseApiResponse.defenseApiResponseBuilder()
                .requests(messageInfos)
                .riskCheckType(curResult.type())
                .riskCheckName(curResult.getSrcName())
                .riskCheckResult(curResult)
                .build();
        response.setRiskCode(curResult.getRiskCode());
        response.setRiskMessage(curResult.getRiskMessage());

        if (StringUtils.isNotEmpty(curResult.getCheckedContent())) {
            response.setCheckedContent(curResult.getCheckedContent());
        } else {
            response.setCheckedContent(context.getCheckContent());
        }
        return response;
    }

    private static List<MessageInfo> convert(List<DefenseApiRequest> reqs) {
        return reqs.stream().map(DefenseApiRequest::getMessageInfo).collect(Collectors.toList());
    }

    public static String fullLog(SessionContext context) {
        String contextLog= "检测全量信息：" + JSON.toJSONString(context, SerializerFeature.DisableCircularReferenceDetect);
        logger.info(contextLog);
        return contextLog;
    }

    // 只保留指定角色的数据
    public static List<DefenseApiResponse> filter(Role fromRole, List<DefenseApiResponse> results) {
        List<DefenseApiResponse> ret = Lists.newArrayList();
        for (DefenseApiResponse res : results) {
            boolean same = true;
            for (MessageInfo messageInfo : res.getRequests()) {
                if (!Objects.equals(fromRole, messageInfo.getFromRole())) {
                    same = false;
                    break;
                }
            }
            if (same) {
                ret.add(res);
            }
        }
        return ret;
    }

    public static RiskCheckResult convert(String resultJson) {
        if (StringUtils.isEmpty(resultJson)) {
            return null;
        }
        try {
            JSONObject jsonObject = JSON.parseObject(resultJson);
            String typeStr = jsonObject.getString("type");
            if (StringUtils.isNotEmpty(typeStr) && RiskCheckType.hasType(typeStr)) {
                return JSON.parseObject(resultJson, RiskCheckType.getClazz(RiskCheckType.valueOf(typeStr)));
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("反序列化失败, json={}", resultJson, e);
            return null;
        }
    }
}
