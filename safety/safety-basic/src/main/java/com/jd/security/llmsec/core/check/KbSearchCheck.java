package com.jd.security.llmsec.core.check;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.List;



@Data
public class KbSearchCheck extends RiskCheckResult{
    @Override
    public RiskCheckType type() {
        return RiskCheckType.kb_search;
    }

    private List<Doc> docs;
    private SingleLabelPredCheck originCheck;

    @Data
    public static class Doc {
        private String id;
        private double score;
        private String text;
        private JSONObject metadata;
    }
}
