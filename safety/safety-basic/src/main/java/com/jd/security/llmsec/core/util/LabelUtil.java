package com.jd.security.llmsec.core.util;

import com.jd.security.llmsec.core.check.RiskInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LabelUtil {
    public static final RiskInfo normalRiskInfo = RiskInfo.builder().riskCode(0).riskMessage("正常文本").probability(1.0f).build();

    /**
     * 0正常文本  => 0, 正常文本
     * 1008歧视内容 => 1008, 歧视内容
     */
    public static RiskInfo parse(String label) {
        Pattern pattern = Pattern.compile("^(\\d+)(.*)$");
        Matcher matcher = pattern.matcher(label);
        if (matcher.matches()) {
            return RiskInfo.builder().riskCode(Integer.valueOf(matcher.group(1)))
                    .riskMessage(matcher.group(2)).build();
        } else {
            return null;
        }
    }

//    public static void main(String[] args) {
//        System.out.println(parse("0正常文本"));
//        System.out.println(parse("1008歧视内容"));
//    }

}
