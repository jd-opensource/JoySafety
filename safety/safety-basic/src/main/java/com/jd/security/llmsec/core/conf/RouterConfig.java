package com.jd.security.llmsec.core.conf;

import lombok.Data;

import java.util.Map;



@Data
public class RouterConfig extends BaseConf{
    private RouterType type;
    private String name;
    private Map<String, Object> conf;
}
