package com.jd.security.llmsec.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.jd.security.llmsec.config.GlobalConf;
import com.jd.security.llmsec.core.ResponseCode;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.openai.ChatCompletion;
import com.jd.security.llmsec.core.openai.ChatCompletionChunk;
import com.jd.security.llmsec.core.openai.Message;
import com.jd.security.llmsec.core.openai.Role;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



public class MessageHelper {
    private static Logger logger = LoggerFactory.getLogger(MessageHelper.class);

    public static String parseOpenAIResp(String content) throws ExceptionWithCode {
        if (content == null) {
            return null;
        }

        content = content.trim();
        if (StringUtils.isEmpty(content)) {
            return content;
        }

        if (content.startsWith("data:")) {
            try {
                content = Splitter.on("data:").trimResults().omitEmptyStrings().splitToList(content)
                        .parallelStream().filter(x -> x.startsWith("{"))
                        .map(x -> JSON.parseObject(x, ChatCompletionChunk.class))
                        .filter(x -> CollectionUtils.isNotEmpty(x.getChoices()))
                        .map(x -> x.getChoices().get(0).getDelta().getContent())
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.joining());
            } catch (Exception e) {
                logger.warn("解析openai流式输出失败, content={}", content, e);
                // throw new ExceptionWithCode(ResponseCode.param_invalid.code, "解析流式返回信息失败");
            }
        } else if (content.startsWith("{")) {
            try {
                ChatCompletion chatCompletion = JSON.parseObject(content, ChatCompletion.class);
                content = chatCompletion.getChoices().get(0).getMessage().getContent();
            } catch (Exception e) {
                logger.warn("解析openai格式的请求消息失败, content={}", content, e);
            }
        }

        content = ContentHelper.cutMaxLen(content);
        return content;
    }

    public static List<Message> parseOpenAIReqMessage(String content) {
        String messageStr = content.trim();
        if (!messageStr.startsWith("[{")) {
            return null;
        }

        try {
            return JSON.parseObject(content, new TypeReference<List<Message>>() {
            });
        } catch (Exception e) {
            logger.error("解析openai格式的请求消息失败", e);
            return null;
        }
    }


    public static String userContent(List<Message> openaiMessages) {
        List<String> userMsgs = openaiMessages.stream().filter(x -> Objects.equals(x.getRole(), Role.user)).map(x -> x.getContent()).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userMsgs)) {
            List<String> notUserMsgs = openaiMessages.stream().filter(x -> !Objects.equals(x.getRole(), Role.user)).map(x -> x.getContent()).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(notUserMsgs)) {
                return "empty";
            }
            return ContentHelper.cutMaxLen(notUserMsgs.get(notUserMsgs.size()-1));
        }

        if (userMsgs.size() > 3) {
            userMsgs = userMsgs.subList(userMsgs.size() - 3, userMsgs.size());
        }

        String ret = Joiner.on("\n").join(userMsgs) + "\n";
        ret = ContentHelper.cutMaxLen(ret);
        return ret;
    }
}
