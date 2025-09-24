package com.jd.security.llmsec.core.engine;

import com.jd.security.llmsec.core.api.defense.AB;
import com.jd.security.llmsec.core.conf.NodeConfig;

import java.util.List;



public interface FunctionChainBuilder {
    Node root(String businessName, AB ab);

    Node refresh(String businessName) throws Exception;

    List<NodeConfig> conf(String businessName);
}
