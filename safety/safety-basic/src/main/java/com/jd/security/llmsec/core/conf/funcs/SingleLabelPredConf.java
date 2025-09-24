package com.jd.security.llmsec.core.conf.funcs;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jd.security.llmsec.core.check.RiskInfo;
import com.jd.security.llmsec.core.openai.ChatRequest;
import com.jd.security.llmsec.core.util.LabelUtil;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;



@Data
public class SingleLabelPredConf {
    private String name;
    private ModelType modelType;
    private Set<Integer> ignoreRiskCode = Sets.newHashSet();
    private RiskInfo defaultRisk = LabelUtil.normalRiskInfo;

    private Map<String,Object> extra;

//    todo 格式对齐为一种
    @Data
    public static class FasttextConf {
        private String url;
        private Map<String,Object> extra = Maps.newHashMap();
    }

    @Data
    public static class BertConf {
//        private String interfaceName;
        private String modelName;
        private String alias;
        private List<String> labels;
//        非空时认为是使用http版本
        private String url;
    }

    @Data
    public static class TextcnnConf {
        private String url;
    }

    @Data
    public static class LlmConf {
        private String url;
        private String authorization;
        private Boolean convertFive2One = Boolean.FALSE;
//        private String model;
//        private Float temperature;
//        private Integer max_tokens;
//        private Boolean stream = Boolean.FALSE;
//        private int timeoutInMilliseconds = 2000;

        private String systemPrompt;
        private String instruction;
        private ChatRequest chatConf;
    }
}
