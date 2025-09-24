package com.jd.security.llmsec.core.conf;

import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.session.Role;
import lombok.Data;

import java.util.Map;



@Data
public class FunctionConfig extends BaseConf{
    private RiskCheckType type;
    // function作用的对象类型，为了编排更灵活
    private Role target;
    private String name;
    private String ref; // 与name对应，可能在其它地方被引用
    private String desc;
    private Integer timeoutMilliseconds;
    private Map<String,Object> conf;
}
