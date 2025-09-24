package com.jd.security.llmsec.core.session;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Lists;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponseX;
import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;



@Data
public class History<T> {
    private static Logger logger = LoggerFactory.getLogger(History.class);
    private HistoryType type;
    private long timestamp;
    private List<T> data;

    public static History fromJson(String json) {
        JSONObject jsonObject = (JSONObject) JSON.parse(json);
        HistoryType type = jsonObject.getObject("type", HistoryType.class);
        if (type == null) {
            return null;
        }

        History history = new History();
        List data = Lists.newArrayList();
        switch (type) {
            case user:
            case robot:
                data = jsonObject.getObject("data", new TypeReference<List<DefenseApiRequest>>() {});
                break;
            case system:
                List<DefenseApiResponseX> responseX = jsonObject.getObject("data", new TypeReference<List<DefenseApiResponseX>>() {});
                data = convert(responseX);
                break;
        }
        history.setData(data);
        history.setType(type);

        Long timestamp = jsonObject.getLong("timestamp");
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }

        history.setTimestamp(timestamp);
        return history;
    }

    public static DefenseApiResponse responseFromJson(String json) {
        DefenseApiResponseX responseX = JSON.parseObject(json, DefenseApiResponseX.class);
        return convert(responseX);
    }

    private static List<DefenseApiResponse> convert(List<DefenseApiResponseX> responseX) {
        List<DefenseApiResponse> result = Lists.newArrayList();
        for (DefenseApiResponseX x : responseX) {
            DefenseApiResponse y = convert(x);
            result.add(y);
        }
        return result;
    }

    public static DefenseApiResponse convert(DefenseApiResponseX x) {
        DefenseApiResponse result = new DefenseApiResponse();
        result.setRequests(x.getRequests());
        result.setRiskCode(x.getRiskCode());
        result.setRiskMessage(x.getRiskMessage());

        String riskCheckTypeStr = x.getRiskCheckType();
        if (StringUtils.isNotEmpty(riskCheckTypeStr) && RiskCheckType.hasType(riskCheckTypeStr)) {
            result.setRiskCheckType(RiskCheckType.valueOf(riskCheckTypeStr));
        }
        result.setRiskCheckName(x.getRiskCheckName());
        result.setCheckedContent(x.getCheckedContent());

        if (result.getRiskCheckType() == null) {
            logger.error("riskCheckType为空，x={}", JSON.toJSONString(x));
            return result;
        }

        Class<? extends RiskCheckResult> clazz = RiskCheckType.getClazz(result.getRiskCheckType());
        RiskCheckResult yResult = JSON.parseObject(JSON.toJSONString(x.getRiskCheckResult()), clazz);
        result.setRiskCheckResult(yResult);
        return result;
    }
}
