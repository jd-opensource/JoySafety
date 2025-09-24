package com.jd.security.llmsec.core.conf.funcs;

import com.google.common.collect.Sets;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;



@Data
public class ParallelConf {
    private List<FunctionConfig> functionConfs;
    private ReduceType reduceType = ReduceType.no_reduce;
    private Set<Integer> ignoreRiskCode = Sets.newHashSet();
    private Map<String,Object> extra;

    public enum ReduceType {
        no_reduce,
        first_risk,
        groovy_script
    }

    @Data
    public static class GroovyConf {
        private String scriptRaw;
        private String script;
    }
}
