package com.jd.security.llmsec.pojo.business;

import com.google.common.collect.Sets;
import com.jd.security.llmsec.core.session.CheckConf;
import com.jd.security.llmsec.data.pojo.BusinessInfoWithBLOBs;
import lombok.Data;

import java.util.Set;



@Data
public class BusinessInfoVO extends BusinessInfoWithBLOBs {
    public static final Set<String> ACCESS_TARGEST = Sets.newHashSet("defenseV2", "defenseResultV2");
    private Set<String> accessTargetsArray = Sets.newHashSet(ACCESS_TARGEST);
    private CheckConf robotCheckConfObj;
    private CheckConf userCheckConfObj;
}
