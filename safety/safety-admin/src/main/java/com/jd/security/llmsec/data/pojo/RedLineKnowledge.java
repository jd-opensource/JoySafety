package com.jd.security.llmsec.data.pojo;

import java.util.Date;

public class RedLineKnowledge {
    private Long id;

    private String uniqId;

    private String businessScene;

    private String className;

    private Long classNo;

    private String source;

    private String type;

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

    public String getUniqId() {
        return uniqId;
    }

    public void setUniqId(String uniqId) {
        this.uniqId = uniqId == null ? null : uniqId.trim();
    }

    public String getBusinessScene() {
        return businessScene;
    }

    public void setBusinessScene(String businessScene) {
        this.businessScene = businessScene == null ? null : businessScene.trim();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className == null ? null : className.trim();
    }

    public Long getClassNo() {
        return classNo;
    }

    public void setClassNo(Long classNo) {
        this.classNo = classNo;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source == null ? null : source.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
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