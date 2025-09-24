package com.jd.security.llmsec.data.pojo;

import java.util.Date;

public class BusinessInfo {
    private Long id;

    private String name;

    private String group;

    private String desc;

    private String type;

    private String secretKey;

    private Integer qpsLimit;

    private Integer version;

    private String status;

    private String editorErp;

    private String editorName;

    private String auditorErp;

    private String auditorName;

    private String releaseErp;

    private String releaseName;

    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group == null ? null : group.trim();
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc == null ? null : desc.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey == null ? null : secretKey.trim();
    }

    public Integer getQpsLimit() {
        return qpsLimit;
    }

    public void setQpsLimit(Integer qpsLimit) {
        this.qpsLimit = qpsLimit;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getEditorErp() {
        return editorErp;
    }

    public void setEditorErp(String editorErp) {
        this.editorErp = editorErp == null ? null : editorErp.trim();
    }

    public String getEditorName() {
        return editorName;
    }

    public void setEditorName(String editorName) {
        this.editorName = editorName == null ? null : editorName.trim();
    }

    public String getAuditorErp() {
        return auditorErp;
    }

    public void setAuditorErp(String auditorErp) {
        this.auditorErp = auditorErp == null ? null : auditorErp.trim();
    }

    public String getAuditorName() {
        return auditorName;
    }

    public void setAuditorName(String auditorName) {
        this.auditorName = auditorName == null ? null : auditorName.trim();
    }

    public String getReleaseErp() {
        return releaseErp;
    }

    public void setReleaseErp(String releaseErp) {
        this.releaseErp = releaseErp == null ? null : releaseErp.trim();
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName == null ? null : releaseName.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}