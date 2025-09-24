package com.jd.security.llmsec.data.pojo;

public class BusinessInfoWithBLOBs extends BusinessInfo {
    private String accessTargets;

    private String robotCheckConf;

    private String userCheckConf;

    public String getAccessTargets() {
        return accessTargets;
    }

    public void setAccessTargets(String accessTargets) {
        this.accessTargets = accessTargets == null ? null : accessTargets.trim();
    }

    public String getRobotCheckConf() {
        return robotCheckConf;
    }

    public void setRobotCheckConf(String robotCheckConf) {
        this.robotCheckConf = robotCheckConf == null ? null : robotCheckConf.trim();
    }

    public String getUserCheckConf() {
        return userCheckConf;
    }

    public void setUserCheckConf(String userCheckConf) {
        this.userCheckConf = userCheckConf == null ? null : userCheckConf.trim();
    }
}