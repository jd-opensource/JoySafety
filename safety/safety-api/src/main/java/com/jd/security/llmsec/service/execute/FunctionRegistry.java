package com.jd.security.llmsec.service.execute;

import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.engine.AbstractFunction;
import com.jd.security.llmsec.core.engine.Function;
import com.jd.security.llmsec.service.execute.functions.*;
import com.jd.security.llmsec.utils.FunctionUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;



public class FunctionRegistry {
    public static final Map<RiskCheckType, Class<? extends AbstractFunction>> FUNC_MAP = new HashMap<RiskCheckType, Class<? extends AbstractFunction>>() {{
        put(RiskCheckType.dummy, DummyFunction.class);
        put(RiskCheckType.keyword, KeywordFunction.class);
        put(RiskCheckType.single_label_pred, SingleLabelPredFunction.class);
        put(RiskCheckType.multi_label_pred, MultiLablePreFunction.class);
        put(RiskCheckType.kb_search, KbSearchFunction.class);
        put(RiskCheckType.rag_answer, RagAnswerFunction.class);
        put(RiskCheckType.multi_turn_detect, MultiTurnDetectFunction.class);
        put(RiskCheckType.parallel, ParallelFunction.class);
        put(RiskCheckType.async, AsyncFunction.class);
    }};

    public static Function buildFunction(FunctionConfig functionConf) throws Exception {
        return FunctionUtil.buildFunction(functionConf);
    }
}
