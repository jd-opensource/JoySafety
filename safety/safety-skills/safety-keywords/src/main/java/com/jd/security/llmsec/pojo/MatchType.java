package com.jd.security.llmsec.pojo;

import com.jd.security.llmsec.data.pojo.SensitiveWords;
import lombok.Data;

import java.util.regex.Pattern;


public enum MatchType {
    /**
     * 包含指定词
     */
    contain,

    /**
     * 完全相等
     */
    equal,

    /**
     * 包含正则指定的内容
     */
    regex,

    /**
     * 排除（用于在特定业务下排除通用的词）
     */
    exclude,
    ;


    @Data
    public static class SensitiveWordsVO extends SensitiveWords {
        private String content;
        private String businessName;
        private String whereClause;
        private Pattern pattern;
    //    private ParsedRule parsedRule;

    }
}