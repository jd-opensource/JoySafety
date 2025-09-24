package com.jd.security.llmsec.utils;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.jd.security.llmsec.pojo.MatchType;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;


public class WordsUtils {
    /*
    1. black > white > grey
    2. 长的 > 短的
     */
    public static Comparator WORDS_Comparator = Comparator.comparing((Function<MatchType.SensitiveWordsVO, Integer>) sensitiveWordsVO -> {
        String handleStrategy = sensitiveWordsVO.getHandleStrategy();
        switch (handleStrategy) {
            case "exclude":
                return -4;
            case "black":
                return -3;
            case "white":
                return -2;
            case "grey":
                return -1;
            default:
                return 0;
        }
    }).thenComparing(sensitiveWordsVO -> -sensitiveWordsVO.getWord().length());


    public static void main(String[] args) {
        List<MatchType.SensitiveWordsVO> vos = Lists.newArrayList();

        MatchType.SensitiveWordsVO vo1 = new MatchType.SensitiveWordsVO();
        vo1.setWord("自慰");
        vo1.setHandleStrategy("black");

        MatchType.SensitiveWordsVO vo2 = new MatchType.SensitiveWordsVO();
        vo2.setWord("自慰高潮");
        vo2.setHandleStrategy("white");


        MatchType.SensitiveWordsVO vo3 = new MatchType.SensitiveWordsVO();
        vo3.setWord("自慰高潮X");
        vo3.setHandleStrategy("white");


        vos.add(vo3);
        vos.add(vo2);
        vos.add(vo1);

        System.out.println(JSON.toJSONString(vos));


        vos.sort(WORDS_Comparator);
        System.out.println(JSON.toJSONString(vos));
    }
}
