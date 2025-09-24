package com.jd.security.llmsec.core.api.defense;

import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.session.History;
import com.jd.security.llmsec.core.session.MessageInfo;
import lombok.Data;

import java.util.List;
import java.util.Objects;



@Data
public class DefenseApiResponseX {
    /*
    ONLY FOR 反序列化
     */

    public DefenseApiResponse toResponse() {
        return History.convert(this);
    }

    /**
     * 对应检测的对象；
     * - 当同步处理时，对应当前请求
     * - 当异步处理时，对应检测窗口内的所有请求数据
     */
    private List<MessageInfo> requests;
    /**
     * 被检测的内容，可能关联了上下文
     */
    private String checkedContent;

    private Integer riskCode = -1;
    private String riskMessage;

    public boolean hasRisk() {
        return !Objects.equals(0, this.riskCode);
    }

    /**
     * 命中的风险检测类型
     *
     * {@link RiskCheckType}
     */
    private String riskCheckType;

    /**
     * 命中的风险检测名(同一个类型可能会有多种具体的实现方法)
     */
    private String riskCheckName;

    /**
     * 具体的识别结果
     */
    private Object riskCheckResult;

    /*
    处置方式
     */
    private String handleStrategy;
}

