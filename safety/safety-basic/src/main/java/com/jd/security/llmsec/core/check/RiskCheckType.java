package com.jd.security.llmsec.core.check;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;



public enum RiskCheckType {
    /**
     * 空类型，啥也不做(内部使用，一般不会返回给用户)
     */
    dummy,

    /**
     * 单标签(只会对输入给出一个分类)预测概率类型
     */
    single_label_pred,

    /**
     * 多标签(会给输入给出多个分类)预测概率类型
     */
    multi_label_pred,

    /**
     * 黑白名单/正则匹配类
     */
    keyword,

    /**
     * 基于知识库搜索
     */
    kb_search,

    /**
     * 基于rag回复
     */
    rag_answer,

    /**
     * 多轮检测
     */
    multi_turn_detect,

    /**
     * 并行执行的check类型(内部使用，一般不会返回给用户)
     */
    parallel,

    /**
     * 异步执行的check类型(wrapper类型，内部执行的是其它检测能力/组合，不会同步返回结果)
     */
    async,

    /**
     * 混合类型，结果可能是基于动态规则和多种检测类型汇总出来的
     */
    mixed;




    private static final Map<RiskCheckType, Class<? extends RiskCheckResult>> map = Maps.newHashMap();
    private static final Set<String> allValues = Sets.newHashSet();
    static {
        map.put(single_label_pred, SingleLabelPredCheck.class);
        map.put(multi_label_pred, MultiLabelPredCheck.class);
        map.put(keyword, KeywordCheck.class);
        map.put(kb_search, KbSearchCheck.class);
        map.put(rag_answer, RagAnswerCheck.class);
        map.put(multi_turn_detect, MultiTurnDetectCheck.class);
        map.put(parallel, ParallelCheckResult.class);
        map.put(async, AsyncCheckResult.class);
        map.put(mixed, MixedCheck.class);

        RiskCheckType[] types = RiskCheckType.values();
        for (RiskCheckType type : types) {
            allValues.add(type.name());
        }
    }

    public static boolean hasType(String type) {
        return allValues.contains(type);
    }

    public static Class<? extends RiskCheckResult> getClazz(RiskCheckType type) {
        return map.get(type);
    }
}
