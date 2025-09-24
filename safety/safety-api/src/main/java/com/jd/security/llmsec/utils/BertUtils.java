package com.jd.security.llmsec.utils;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class BertUtils {
    private static Logger logger = LoggerFactory.getLogger(BertUtils.class);
    public final static Set<String> fullSentenceEnds = Sets.newHashSet("？", "?", "!", "！", "。");

    public static final int BERT_LEN = 512;

    /**
     * 1. 去除右侧半句话
     * 2. 在1基础上，如果不足512时，取所有；如果超出512，去年左侧整句
     *
     * @param fullCheckContent
     * @return
     */
    public static String cut(String fullCheckContent) {
        int lastEndIndex = -1;
        for (String end : fullSentenceEnds) {
            int i = fullCheckContent.lastIndexOf(end);
            if (i > lastEndIndex) {
                lastEndIndex = i;
            }
        }
        if (lastEndIndex != -1) {
            fullCheckContent = fullCheckContent.substring(0, lastEndIndex + 1);
        }

        if (fullCheckContent.length() <= 512) {
            return fullCheckContent;
        } else {
            fullCheckContent = fullCheckContent.substring(fullCheckContent.length() - BERT_LEN);

            int firstEndIndex = Integer.MAX_VALUE;
            for (String end : fullSentenceEnds) {
                int i = fullCheckContent.indexOf(end);
                if (i != -1 && i < firstEndIndex) {
                    firstEndIndex = i;
                }
            }
            if (firstEndIndex != Integer.MAX_VALUE && firstEndIndex < fullCheckContent.length() -1) {
                fullCheckContent = fullCheckContent.substring(firstEndIndex + 1);
            }
        }

        return fullCheckContent;
    }
}
