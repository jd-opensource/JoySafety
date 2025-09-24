package com.jd.security.llmsec.data.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RedLineKnowledgeExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public RedLineKnowledgeExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("`id` is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("`id` is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("`id` =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("`id` <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("`id` >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("`id` >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("`id` <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("`id` <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("`id` in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("`id` not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("`id` between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("`id` not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andUniqIdIsNull() {
            addCriterion("`uniq_id` is null");
            return (Criteria) this;
        }

        public Criteria andUniqIdIsNotNull() {
            addCriterion("`uniq_id` is not null");
            return (Criteria) this;
        }

        public Criteria andUniqIdEqualTo(String value) {
            addCriterion("`uniq_id` =", value, "uniqId");
            return (Criteria) this;
        }

        public Criteria andUniqIdNotEqualTo(String value) {
            addCriterion("`uniq_id` <>", value, "uniqId");
            return (Criteria) this;
        }

        public Criteria andUniqIdGreaterThan(String value) {
            addCriterion("`uniq_id` >", value, "uniqId");
            return (Criteria) this;
        }

        public Criteria andUniqIdGreaterThanOrEqualTo(String value) {
            addCriterion("`uniq_id` >=", value, "uniqId");
            return (Criteria) this;
        }

        public Criteria andUniqIdLessThan(String value) {
            addCriterion("`uniq_id` <", value, "uniqId");
            return (Criteria) this;
        }

        public Criteria andUniqIdLessThanOrEqualTo(String value) {
            addCriterion("`uniq_id` <=", value, "uniqId");
            return (Criteria) this;
        }

        public Criteria andUniqIdLike(String value) {
            addCriterion("`uniq_id` like", value, "uniqId");
            return (Criteria) this;
        }

        public Criteria andUniqIdNotLike(String value) {
            addCriterion("`uniq_id` not like", value, "uniqId");
            return (Criteria) this;
        }

        public Criteria andUniqIdIn(List<String> values) {
            addCriterion("`uniq_id` in", values, "uniqId");
            return (Criteria) this;
        }

        public Criteria andUniqIdNotIn(List<String> values) {
            addCriterion("`uniq_id` not in", values, "uniqId");
            return (Criteria) this;
        }

        public Criteria andUniqIdBetween(String value1, String value2) {
            addCriterion("`uniq_id` between", value1, value2, "uniqId");
            return (Criteria) this;
        }

        public Criteria andUniqIdNotBetween(String value1, String value2) {
            addCriterion("`uniq_id` not between", value1, value2, "uniqId");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneIsNull() {
            addCriterion("`business_scene` is null");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneIsNotNull() {
            addCriterion("`business_scene` is not null");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneEqualTo(String value) {
            addCriterion("`business_scene` =", value, "businessScene");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneNotEqualTo(String value) {
            addCriterion("`business_scene` <>", value, "businessScene");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneGreaterThan(String value) {
            addCriterion("`business_scene` >", value, "businessScene");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneGreaterThanOrEqualTo(String value) {
            addCriterion("`business_scene` >=", value, "businessScene");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneLessThan(String value) {
            addCriterion("`business_scene` <", value, "businessScene");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneLessThanOrEqualTo(String value) {
            addCriterion("`business_scene` <=", value, "businessScene");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneLike(String value) {
            addCriterion("`business_scene` like", value, "businessScene");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneNotLike(String value) {
            addCriterion("`business_scene` not like", value, "businessScene");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneIn(List<String> values) {
            addCriterion("`business_scene` in", values, "businessScene");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneNotIn(List<String> values) {
            addCriterion("`business_scene` not in", values, "businessScene");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneBetween(String value1, String value2) {
            addCriterion("`business_scene` between", value1, value2, "businessScene");
            return (Criteria) this;
        }

        public Criteria andBusinessSceneNotBetween(String value1, String value2) {
            addCriterion("`business_scene` not between", value1, value2, "businessScene");
            return (Criteria) this;
        }

        public Criteria andClassNameIsNull() {
            addCriterion("`class_name` is null");
            return (Criteria) this;
        }

        public Criteria andClassNameIsNotNull() {
            addCriterion("`class_name` is not null");
            return (Criteria) this;
        }

        public Criteria andClassNameEqualTo(String value) {
            addCriterion("`class_name` =", value, "className");
            return (Criteria) this;
        }

        public Criteria andClassNameNotEqualTo(String value) {
            addCriterion("`class_name` <>", value, "className");
            return (Criteria) this;
        }

        public Criteria andClassNameGreaterThan(String value) {
            addCriterion("`class_name` >", value, "className");
            return (Criteria) this;
        }

        public Criteria andClassNameGreaterThanOrEqualTo(String value) {
            addCriterion("`class_name` >=", value, "className");
            return (Criteria) this;
        }

        public Criteria andClassNameLessThan(String value) {
            addCriterion("`class_name` <", value, "className");
            return (Criteria) this;
        }

        public Criteria andClassNameLessThanOrEqualTo(String value) {
            addCriterion("`class_name` <=", value, "className");
            return (Criteria) this;
        }

        public Criteria andClassNameLike(String value) {
            addCriterion("`class_name` like", value, "className");
            return (Criteria) this;
        }

        public Criteria andClassNameNotLike(String value) {
            addCriterion("`class_name` not like", value, "className");
            return (Criteria) this;
        }

        public Criteria andClassNameIn(List<String> values) {
            addCriterion("`class_name` in", values, "className");
            return (Criteria) this;
        }

        public Criteria andClassNameNotIn(List<String> values) {
            addCriterion("`class_name` not in", values, "className");
            return (Criteria) this;
        }

        public Criteria andClassNameBetween(String value1, String value2) {
            addCriterion("`class_name` between", value1, value2, "className");
            return (Criteria) this;
        }

        public Criteria andClassNameNotBetween(String value1, String value2) {
            addCriterion("`class_name` not between", value1, value2, "className");
            return (Criteria) this;
        }

        public Criteria andClassNoIsNull() {
            addCriterion("`class_no` is null");
            return (Criteria) this;
        }

        public Criteria andClassNoIsNotNull() {
            addCriterion("`class_no` is not null");
            return (Criteria) this;
        }

        public Criteria andClassNoEqualTo(Long value) {
            addCriterion("`class_no` =", value, "classNo");
            return (Criteria) this;
        }

        public Criteria andClassNoNotEqualTo(Long value) {
            addCriterion("`class_no` <>", value, "classNo");
            return (Criteria) this;
        }

        public Criteria andClassNoGreaterThan(Long value) {
            addCriterion("`class_no` >", value, "classNo");
            return (Criteria) this;
        }

        public Criteria andClassNoGreaterThanOrEqualTo(Long value) {
            addCriterion("`class_no` >=", value, "classNo");
            return (Criteria) this;
        }

        public Criteria andClassNoLessThan(Long value) {
            addCriterion("`class_no` <", value, "classNo");
            return (Criteria) this;
        }

        public Criteria andClassNoLessThanOrEqualTo(Long value) {
            addCriterion("`class_no` <=", value, "classNo");
            return (Criteria) this;
        }

        public Criteria andClassNoIn(List<Long> values) {
            addCriterion("`class_no` in", values, "classNo");
            return (Criteria) this;
        }

        public Criteria andClassNoNotIn(List<Long> values) {
            addCriterion("`class_no` not in", values, "classNo");
            return (Criteria) this;
        }

        public Criteria andClassNoBetween(Long value1, Long value2) {
            addCriterion("`class_no` between", value1, value2, "classNo");
            return (Criteria) this;
        }

        public Criteria andClassNoNotBetween(Long value1, Long value2) {
            addCriterion("`class_no` not between", value1, value2, "classNo");
            return (Criteria) this;
        }

        public Criteria andSourceIsNull() {
            addCriterion("`source` is null");
            return (Criteria) this;
        }

        public Criteria andSourceIsNotNull() {
            addCriterion("`source` is not null");
            return (Criteria) this;
        }

        public Criteria andSourceEqualTo(String value) {
            addCriterion("`source` =", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceNotEqualTo(String value) {
            addCriterion("`source` <>", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceGreaterThan(String value) {
            addCriterion("`source` >", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceGreaterThanOrEqualTo(String value) {
            addCriterion("`source` >=", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceLessThan(String value) {
            addCriterion("`source` <", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceLessThanOrEqualTo(String value) {
            addCriterion("`source` <=", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceLike(String value) {
            addCriterion("`source` like", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceNotLike(String value) {
            addCriterion("`source` not like", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceIn(List<String> values) {
            addCriterion("`source` in", values, "source");
            return (Criteria) this;
        }

        public Criteria andSourceNotIn(List<String> values) {
            addCriterion("`source` not in", values, "source");
            return (Criteria) this;
        }

        public Criteria andSourceBetween(String value1, String value2) {
            addCriterion("`source` between", value1, value2, "source");
            return (Criteria) this;
        }

        public Criteria andSourceNotBetween(String value1, String value2) {
            addCriterion("`source` not between", value1, value2, "source");
            return (Criteria) this;
        }

        public Criteria andTypeIsNull() {
            addCriterion("`type` is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("`type` is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(String value) {
            addCriterion("`type` =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(String value) {
            addCriterion("`type` <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(String value) {
            addCriterion("`type` >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("`type` >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(String value) {
            addCriterion("`type` <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("`type` <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLike(String value) {
            addCriterion("`type` like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotLike(String value) {
            addCriterion("`type` not like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<String> values) {
            addCriterion("`type` in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<String> values) {
            addCriterion("`type` not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(String value1, String value2) {
            addCriterion("`type` between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(String value1, String value2) {
            addCriterion("`type` not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andVersionIsNull() {
            addCriterion("`version` is null");
            return (Criteria) this;
        }

        public Criteria andVersionIsNotNull() {
            addCriterion("`version` is not null");
            return (Criteria) this;
        }

        public Criteria andVersionEqualTo(Integer value) {
            addCriterion("`version` =", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(Integer value) {
            addCriterion("`version` <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(Integer value) {
            addCriterion("`version` >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(Integer value) {
            addCriterion("`version` >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(Integer value) {
            addCriterion("`version` <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(Integer value) {
            addCriterion("`version` <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionIn(List<Integer> values) {
            addCriterion("`version` in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotIn(List<Integer> values) {
            addCriterion("`version` not in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionBetween(Integer value1, Integer value2) {
            addCriterion("`version` between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotBetween(Integer value1, Integer value2) {
            addCriterion("`version` not between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("`status` is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("`status` is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("`status` like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("`status` not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("`status` not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andEditorErpIsNull() {
            addCriterion("`editor_erp` is null");
            return (Criteria) this;
        }

        public Criteria andEditorErpIsNotNull() {
            addCriterion("`editor_erp` is not null");
            return (Criteria) this;
        }

        public Criteria andEditorErpEqualTo(String value) {
            addCriterion("`editor_erp` =", value, "editorErp");
            return (Criteria) this;
        }

        public Criteria andEditorErpNotEqualTo(String value) {
            addCriterion("`editor_erp` <>", value, "editorErp");
            return (Criteria) this;
        }

        public Criteria andEditorErpGreaterThan(String value) {
            addCriterion("`editor_erp` >", value, "editorErp");
            return (Criteria) this;
        }

        public Criteria andEditorErpGreaterThanOrEqualTo(String value) {
            addCriterion("`editor_erp` >=", value, "editorErp");
            return (Criteria) this;
        }

        public Criteria andEditorErpLessThan(String value) {
            addCriterion("`editor_erp` <", value, "editorErp");
            return (Criteria) this;
        }

        public Criteria andEditorErpLessThanOrEqualTo(String value) {
            addCriterion("`editor_erp` <=", value, "editorErp");
            return (Criteria) this;
        }

        public Criteria andEditorErpLike(String value) {
            addCriterion("`editor_erp` like", value, "editorErp");
            return (Criteria) this;
        }

        public Criteria andEditorErpNotLike(String value) {
            addCriterion("`editor_erp` not like", value, "editorErp");
            return (Criteria) this;
        }

        public Criteria andEditorErpIn(List<String> values) {
            addCriterion("`editor_erp` in", values, "editorErp");
            return (Criteria) this;
        }

        public Criteria andEditorErpNotIn(List<String> values) {
            addCriterion("`editor_erp` not in", values, "editorErp");
            return (Criteria) this;
        }

        public Criteria andEditorErpBetween(String value1, String value2) {
            addCriterion("`editor_erp` between", value1, value2, "editorErp");
            return (Criteria) this;
        }

        public Criteria andEditorErpNotBetween(String value1, String value2) {
            addCriterion("`editor_erp` not between", value1, value2, "editorErp");
            return (Criteria) this;
        }

        public Criteria andEditorNameIsNull() {
            addCriterion("`editor_name` is null");
            return (Criteria) this;
        }

        public Criteria andEditorNameIsNotNull() {
            addCriterion("`editor_name` is not null");
            return (Criteria) this;
        }

        public Criteria andEditorNameEqualTo(String value) {
            addCriterion("`editor_name` =", value, "editorName");
            return (Criteria) this;
        }

        public Criteria andEditorNameNotEqualTo(String value) {
            addCriterion("`editor_name` <>", value, "editorName");
            return (Criteria) this;
        }

        public Criteria andEditorNameGreaterThan(String value) {
            addCriterion("`editor_name` >", value, "editorName");
            return (Criteria) this;
        }

        public Criteria andEditorNameGreaterThanOrEqualTo(String value) {
            addCriterion("`editor_name` >=", value, "editorName");
            return (Criteria) this;
        }

        public Criteria andEditorNameLessThan(String value) {
            addCriterion("`editor_name` <", value, "editorName");
            return (Criteria) this;
        }

        public Criteria andEditorNameLessThanOrEqualTo(String value) {
            addCriterion("`editor_name` <=", value, "editorName");
            return (Criteria) this;
        }

        public Criteria andEditorNameLike(String value) {
            addCriterion("`editor_name` like", value, "editorName");
            return (Criteria) this;
        }

        public Criteria andEditorNameNotLike(String value) {
            addCriterion("`editor_name` not like", value, "editorName");
            return (Criteria) this;
        }

        public Criteria andEditorNameIn(List<String> values) {
            addCriterion("`editor_name` in", values, "editorName");
            return (Criteria) this;
        }

        public Criteria andEditorNameNotIn(List<String> values) {
            addCriterion("`editor_name` not in", values, "editorName");
            return (Criteria) this;
        }

        public Criteria andEditorNameBetween(String value1, String value2) {
            addCriterion("`editor_name` between", value1, value2, "editorName");
            return (Criteria) this;
        }

        public Criteria andEditorNameNotBetween(String value1, String value2) {
            addCriterion("`editor_name` not between", value1, value2, "editorName");
            return (Criteria) this;
        }

        public Criteria andAuditorErpIsNull() {
            addCriterion("`auditor_erp` is null");
            return (Criteria) this;
        }

        public Criteria andAuditorErpIsNotNull() {
            addCriterion("`auditor_erp` is not null");
            return (Criteria) this;
        }

        public Criteria andAuditorErpEqualTo(String value) {
            addCriterion("`auditor_erp` =", value, "auditorErp");
            return (Criteria) this;
        }

        public Criteria andAuditorErpNotEqualTo(String value) {
            addCriterion("`auditor_erp` <>", value, "auditorErp");
            return (Criteria) this;
        }

        public Criteria andAuditorErpGreaterThan(String value) {
            addCriterion("`auditor_erp` >", value, "auditorErp");
            return (Criteria) this;
        }

        public Criteria andAuditorErpGreaterThanOrEqualTo(String value) {
            addCriterion("`auditor_erp` >=", value, "auditorErp");
            return (Criteria) this;
        }

        public Criteria andAuditorErpLessThan(String value) {
            addCriterion("`auditor_erp` <", value, "auditorErp");
            return (Criteria) this;
        }

        public Criteria andAuditorErpLessThanOrEqualTo(String value) {
            addCriterion("`auditor_erp` <=", value, "auditorErp");
            return (Criteria) this;
        }

        public Criteria andAuditorErpLike(String value) {
            addCriterion("`auditor_erp` like", value, "auditorErp");
            return (Criteria) this;
        }

        public Criteria andAuditorErpNotLike(String value) {
            addCriterion("`auditor_erp` not like", value, "auditorErp");
            return (Criteria) this;
        }

        public Criteria andAuditorErpIn(List<String> values) {
            addCriterion("`auditor_erp` in", values, "auditorErp");
            return (Criteria) this;
        }

        public Criteria andAuditorErpNotIn(List<String> values) {
            addCriterion("`auditor_erp` not in", values, "auditorErp");
            return (Criteria) this;
        }

        public Criteria andAuditorErpBetween(String value1, String value2) {
            addCriterion("`auditor_erp` between", value1, value2, "auditorErp");
            return (Criteria) this;
        }

        public Criteria andAuditorErpNotBetween(String value1, String value2) {
            addCriterion("`auditor_erp` not between", value1, value2, "auditorErp");
            return (Criteria) this;
        }

        public Criteria andAuditorNameIsNull() {
            addCriterion("`auditor_name` is null");
            return (Criteria) this;
        }

        public Criteria andAuditorNameIsNotNull() {
            addCriterion("`auditor_name` is not null");
            return (Criteria) this;
        }

        public Criteria andAuditorNameEqualTo(String value) {
            addCriterion("`auditor_name` =", value, "auditorName");
            return (Criteria) this;
        }

        public Criteria andAuditorNameNotEqualTo(String value) {
            addCriterion("`auditor_name` <>", value, "auditorName");
            return (Criteria) this;
        }

        public Criteria andAuditorNameGreaterThan(String value) {
            addCriterion("`auditor_name` >", value, "auditorName");
            return (Criteria) this;
        }

        public Criteria andAuditorNameGreaterThanOrEqualTo(String value) {
            addCriterion("`auditor_name` >=", value, "auditorName");
            return (Criteria) this;
        }

        public Criteria andAuditorNameLessThan(String value) {
            addCriterion("`auditor_name` <", value, "auditorName");
            return (Criteria) this;
        }

        public Criteria andAuditorNameLessThanOrEqualTo(String value) {
            addCriterion("`auditor_name` <=", value, "auditorName");
            return (Criteria) this;
        }

        public Criteria andAuditorNameLike(String value) {
            addCriterion("`auditor_name` like", value, "auditorName");
            return (Criteria) this;
        }

        public Criteria andAuditorNameNotLike(String value) {
            addCriterion("`auditor_name` not like", value, "auditorName");
            return (Criteria) this;
        }

        public Criteria andAuditorNameIn(List<String> values) {
            addCriterion("`auditor_name` in", values, "auditorName");
            return (Criteria) this;
        }

        public Criteria andAuditorNameNotIn(List<String> values) {
            addCriterion("`auditor_name` not in", values, "auditorName");
            return (Criteria) this;
        }

        public Criteria andAuditorNameBetween(String value1, String value2) {
            addCriterion("`auditor_name` between", value1, value2, "auditorName");
            return (Criteria) this;
        }

        public Criteria andAuditorNameNotBetween(String value1, String value2) {
            addCriterion("`auditor_name` not between", value1, value2, "auditorName");
            return (Criteria) this;
        }

        public Criteria andReleaseErpIsNull() {
            addCriterion("`release_erp` is null");
            return (Criteria) this;
        }

        public Criteria andReleaseErpIsNotNull() {
            addCriterion("`release_erp` is not null");
            return (Criteria) this;
        }

        public Criteria andReleaseErpEqualTo(String value) {
            addCriterion("`release_erp` =", value, "releaseErp");
            return (Criteria) this;
        }

        public Criteria andReleaseErpNotEqualTo(String value) {
            addCriterion("`release_erp` <>", value, "releaseErp");
            return (Criteria) this;
        }

        public Criteria andReleaseErpGreaterThan(String value) {
            addCriterion("`release_erp` >", value, "releaseErp");
            return (Criteria) this;
        }

        public Criteria andReleaseErpGreaterThanOrEqualTo(String value) {
            addCriterion("`release_erp` >=", value, "releaseErp");
            return (Criteria) this;
        }

        public Criteria andReleaseErpLessThan(String value) {
            addCriterion("`release_erp` <", value, "releaseErp");
            return (Criteria) this;
        }

        public Criteria andReleaseErpLessThanOrEqualTo(String value) {
            addCriterion("`release_erp` <=", value, "releaseErp");
            return (Criteria) this;
        }

        public Criteria andReleaseErpLike(String value) {
            addCriterion("`release_erp` like", value, "releaseErp");
            return (Criteria) this;
        }

        public Criteria andReleaseErpNotLike(String value) {
            addCriterion("`release_erp` not like", value, "releaseErp");
            return (Criteria) this;
        }

        public Criteria andReleaseErpIn(List<String> values) {
            addCriterion("`release_erp` in", values, "releaseErp");
            return (Criteria) this;
        }

        public Criteria andReleaseErpNotIn(List<String> values) {
            addCriterion("`release_erp` not in", values, "releaseErp");
            return (Criteria) this;
        }

        public Criteria andReleaseErpBetween(String value1, String value2) {
            addCriterion("`release_erp` between", value1, value2, "releaseErp");
            return (Criteria) this;
        }

        public Criteria andReleaseErpNotBetween(String value1, String value2) {
            addCriterion("`release_erp` not between", value1, value2, "releaseErp");
            return (Criteria) this;
        }

        public Criteria andReleaseNameIsNull() {
            addCriterion("`release_name` is null");
            return (Criteria) this;
        }

        public Criteria andReleaseNameIsNotNull() {
            addCriterion("`release_name` is not null");
            return (Criteria) this;
        }

        public Criteria andReleaseNameEqualTo(String value) {
            addCriterion("`release_name` =", value, "releaseName");
            return (Criteria) this;
        }

        public Criteria andReleaseNameNotEqualTo(String value) {
            addCriterion("`release_name` <>", value, "releaseName");
            return (Criteria) this;
        }

        public Criteria andReleaseNameGreaterThan(String value) {
            addCriterion("`release_name` >", value, "releaseName");
            return (Criteria) this;
        }

        public Criteria andReleaseNameGreaterThanOrEqualTo(String value) {
            addCriterion("`release_name` >=", value, "releaseName");
            return (Criteria) this;
        }

        public Criteria andReleaseNameLessThan(String value) {
            addCriterion("`release_name` <", value, "releaseName");
            return (Criteria) this;
        }

        public Criteria andReleaseNameLessThanOrEqualTo(String value) {
            addCriterion("`release_name` <=", value, "releaseName");
            return (Criteria) this;
        }

        public Criteria andReleaseNameLike(String value) {
            addCriterion("`release_name` like", value, "releaseName");
            return (Criteria) this;
        }

        public Criteria andReleaseNameNotLike(String value) {
            addCriterion("`release_name` not like", value, "releaseName");
            return (Criteria) this;
        }

        public Criteria andReleaseNameIn(List<String> values) {
            addCriterion("`release_name` in", values, "releaseName");
            return (Criteria) this;
        }

        public Criteria andReleaseNameNotIn(List<String> values) {
            addCriterion("`release_name` not in", values, "releaseName");
            return (Criteria) this;
        }

        public Criteria andReleaseNameBetween(String value1, String value2) {
            addCriterion("`release_name` between", value1, value2, "releaseName");
            return (Criteria) this;
        }

        public Criteria andReleaseNameNotBetween(String value1, String value2) {
            addCriterion("`release_name` not between", value1, value2, "releaseName");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("`create_time` is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("`create_time` is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("`create_time` =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("`create_time` <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("`create_time` >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("`create_time` >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("`create_time` <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("`create_time` <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("`create_time` in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("`create_time` not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("`create_time` between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("`create_time` not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("`update_time` is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("`update_time` is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("`update_time` =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("`update_time` <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("`update_time` >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("`update_time` >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("`update_time` <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("`update_time` <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("`update_time` in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("`update_time` not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("`update_time` between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("`update_time` not between", value1, value2, "updateTime");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}