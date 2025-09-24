package com.jd.security.llmsec.core;

import com.jd.security.llmsec.core.auth.AuthControlConf;
import com.jd.security.llmsec.core.session.BusinessType;
import com.jd.security.llmsec.core.session.CheckConf;
import com.jd.security.llmsec.core.session.ResponseMode;
import lombok.Data;



@Data
public class BusinessConf extends AuthControlConf {
    private String name;
    private String desc;
    private BusinessType type;
    private CheckConf robotCheckConf;
    private CheckConf userCheckConf;
    private String nodeConfFile;
    private String chainRootId;
}
