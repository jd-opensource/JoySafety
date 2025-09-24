package com.jd.security.llmsec.core.conf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.security.llmsec.core.BusinessConf;
import lombok.Data;

import java.util.List;
import java.util.Map;



@Data
public class ConfigResponse {
    private List<Item> confs = Lists.newArrayList();
    // function是全局共享的
    private Map<String, FunctionConfig> functionMap = Maps.newHashMap();

    @Data
    public static class Item {
        private BusinessConf businessConf;
        private String rootNode;
        private List<NodeConfig> nodeConfigs = Lists.newArrayList();
        private Long lastChangeTimestamp;

    }
}


