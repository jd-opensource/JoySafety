package com.jd.security.llmsec.core.session;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.security.llmsec.core.BusinessConf;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.Map;


@Data
public class SessionContext {
    private BusinessConf businessConf;
    private List<History> histories;
    private String checkContent;
    private List<DefenseApiRequest> curReq;
    private DefenseApiRequest lastUserReq;
    private RiskCheckResult curResult;
    private String curFunctionName;
    private Map<RiskCheckType, List<RiskCheckResult>> middleResults = Maps.newHashMap();
    private long executeStartTime = System.currentTimeMillis();
    private long executeEndTime;
    private String endReason;
    private long executeCost;
    private long recvExecuteCost;

    public boolean timeout() {
        Long timeoutConf = curReq.get(0).fromRobot() ? businessConf.getRobotCheckConf().getTimeoutMilliseconds() : businessConf.getUserCheckConf().getTimeoutMilliseconds();
        return System.currentTimeMillis() - executeStartTime > timeoutConf;
    }

    public synchronized void appendResult(RiskCheckResult result) {
        List<RiskCheckResult> results = middleResults.get(result.type());
        if (CollectionUtils.isEmpty(results)) {
            results = Lists.newArrayList();
            middleResults.put(result.type(), results);
        }
        results.add(result);
    }

    public List<RiskCheckResult> resultsByType(RiskCheckType type) {
        return middleResults.get(type);
    }
}
