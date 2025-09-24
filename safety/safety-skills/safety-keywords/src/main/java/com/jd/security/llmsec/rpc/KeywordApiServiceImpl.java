package com.jd.security.llmsec.rpc;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Preconditions;
import com.jd.security.llmsec.core.rpc.KeywordApiService;
import com.jd.security.llmsec.pojo.MatchType;
import com.jd.security.llmsec.pojo.StatusCode;
import com.jd.security.llmsec.service.data.SensitiveWordsManageService;
import com.jd.security.llmsec.utils.BwgLabelUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("KeywordApiServiceImpl")
public class KeywordApiServiceImpl implements KeywordApiService {
    private Logger logger = LoggerFactory.getLogger(KeywordApiServiceImpl.class);
    // todo 支持更长的
    private int MAX_LEN = 10000;

    @Autowired
    private SensitiveWordsManageService sensitiveWordsManageService;

    @Override
    public KVResponse invoke(KVRequest request) {
        long startTimeMillis = System.currentTimeMillis();

        KVResponse ret = null;
        try {
            String content = request.getContent();
            if (StringUtils.isEmpty(content)) {
                ret = new KVResponse(StatusCode.SUCCESS.getCode(), "",0, "正常文本", 0, "正常文本", 0, 1);
                logger.warn("文本为空，req={}", JSON.toJSONString(request));
                return responseLog(request, ret, startTimeMillis);
            }
            content = content.trim();
            if (StringUtils.isEmpty(content)) {
                logger.warn("文本为空，req={}", JSON.toJSONString(request));
                return responseLog(request, new KVResponse(StatusCode.SUCCESS.getCode(), "", 0, "正常文本", 0, "正常文本", 0, 1), startTimeMillis);
            }

            Preconditions.checkArgument(StringUtils.isNotEmpty(content));
            request.setContent(content);
            int length = content.length();
            if (length > MAX_LEN) {
                logger.warn("请求输出长度，length={}, req={}", length, JSON.toJSONString(request));
                content = content.substring(0, MAX_LEN/2) + content.substring(length - MAX_LEN/2, length);
                request.setContent(content);
            }

            MatchType.SensitiveWordsVO result = sensitiveWordsManageService.check(request);

            if (result == null) {
                return responseLog(request, new KVResponse(StatusCode.SUCCESS.getCode(), "",
                        0, "正常文本", 0, "正常文本", 0, System.currentTimeMillis() - startTimeMillis
                ), startTimeMillis);
            } else {
                return responseLog(request, new KVResponse(StatusCode.SUCCESS.getCode(), result.getWord(),
                        result.getFirstClassNo(), result.getFirstClassName(), result.getSecondClassNo(), result.getSecondClassName(),
                        BwgLabelUtils.getBwgLabelFromHandleStrategy(result.getHandleStrategy()), System.currentTimeMillis() - startTimeMillis), startTimeMillis);
            }
        } catch (Exception e) {
            logger.error("敏感词检测失败, req={}", JSON.toJSONString(request));
            return responseLog(request, new KVResponse(StatusCode.INTERNAL_SERVER_ERROR.getCode(), "",
                    0, "正常文本", 0, "正常文本", 0, System.currentTimeMillis() - startTimeMillis), startTimeMillis);
        }
    }

    private KVResponse responseLog(KVRequest vo, KVResponse ret, long startTimeMillis) {
        logger.info("req={}, resp={}, cost={}", JSON.toJSONString(vo), JSON.toJSONString(ret), System.currentTimeMillis() - startTimeMillis);
        return ret;
    }
}
