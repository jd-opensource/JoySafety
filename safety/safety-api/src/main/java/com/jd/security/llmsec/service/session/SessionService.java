package com.jd.security.llmsec.service.session;

import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.openai.Message;
import com.jd.security.llmsec.core.session.History;
import com.jd.security.llmsec.core.session.SessionContext;

import java.util.List;



public interface SessionService {
    String SESSION_HISTORY_PREFIX = "session:his:";
    int HISTORY_EXPIRE_SECONDS = 1800;


    /**
     * 获取最新的
     *
     * @param businessName
     * @param sessionId    会话ID
     * @param messageId    消息ID
     * @param checkNum     检查数量
     * @param context
     * @return 历史记录列表
     */
    List<History> latestSlice(String businessName, String sessionId, Integer messageId, int checkNum, boolean fromUser, SessionContext context) throws Exception;
    List<History> latestSliceWithoutCache(String businessName, String sessionId, Integer messageId, int checkNum) throws Exception;

    // 获取指定消息id所有的分片
    List<DefenseApiRequest> latestSliceWithoutCache(String businessName, String sessionId, Integer messageId) throws Exception;

    List<History> latestMessage(String businessName, String sessionId, int checkNum, boolean fromUser, SessionContext context) throws Exception;
    List<History> latestMessageWithoutCache(String businessName, String sessionId, int checkNum, boolean fromUser) throws Exception;


    void addHistory(SessionContext context, String businessName, String sessionId, List<History> histories) throws Exception;

    void addHistoryV2(String businessName, String sessionId, DefenseApiRequest request, Message message) throws Exception;
    List<Message> latestHistoryV2(String businessName, String sessionId, int count) throws Exception;
}
