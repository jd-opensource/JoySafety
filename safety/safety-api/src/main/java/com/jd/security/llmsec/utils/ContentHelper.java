package com.jd.security.llmsec.utils;

import com.google.common.collect.Lists;
import com.jd.security.llmsec.config.GlobalConf;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.session.History;
import com.jd.security.llmsec.core.session.HistoryType;
import com.jd.security.llmsec.core.session.SessionContext;
import com.jd.security.llmsec.service.session.SessionService;
import com.jd.security.llmsec.service.task.TaskExecutor;
import com.jd.security.llmsec.service.task.TaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;



public class ContentHelper {
    public static String respCacheKey(String businessName, String content) {
        return GlobalConf.CACHE_PREFIX + businessName + ":" + Md5Util.digest(content);
    }

    public static String historyKeyV2(String businessName, String sessionId) {
        return GlobalConf.CACHE_PREFIX + ":" + SessionService.SESSION_HISTORY_PREFIX + businessName + ":" + sessionId;
    }

    public static String historyKey(String sessionId) {
        return GlobalConf.CACHE_PREFIX + SessionService.SESSION_HISTORY_PREFIX + sessionId;
    }

    public static String responseKey(String sessionId) {
        return GlobalConf.CACHE_PREFIX + TaskExecutor.SESSION_EXECUTE_RESP_PREFIX + sessionId;
    }

    public static String lockKey(String sessionId) {
        return GlobalConf.CACHE_PREFIX + TaskExecutor.SESSION_EXECUTE_LOCK_PREFIX + sessionId;
    }

    public static String taskQueueKey() {
        return GlobalConf.CACHE_PREFIX + TaskService.TASK_QUEUE;
    }

    public static String newTaskKey(String sessionId) {
        return GlobalConf.CACHE_PREFIX + TaskService.SESSION_NEW_MESG_PREFIX + sessionId;
    }

    public static String globalSessionKey(String businessName, String sessionId) {
        return businessName + ":" + sessionId;
    }

    public static String join2NormalStrWithOrder(SessionContext context) {
        List<DefenseApiRequest> allRequests = Lists.newArrayList();
        List<History> histories = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(context.getHistories())) {
            histories = context.getHistories().stream().filter(x -> !Objects.equals(x.getType(), HistoryType.system)).collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(histories)) {
            for (History h : histories) {
                allRequests.addAll((List<DefenseApiRequest>)h.getData());
            }
        }
        if (CollectionUtils.isNotEmpty(context.getCurReq())) {
            allRequests.addAll(context.getCurReq());
        }
        if (CollectionUtils.isEmpty(allRequests)) {
            return null;
        }

        DefenseApiRequest firstReq = allRequests.get(0);
        Integer messageId = firstReq.getMessageInfo().getMessageId();
        Integer sliceId = firstReq.getMessageInfo().getSliceId();
        if (firstReq.fromRobot()) {
            if (sliceId == null) {
                allRequests.sort(timestampComparator());
            } else {
                allRequests.sort(sliceIdComparator());
            }
        } else {
            if (messageId == null) {
                allRequests.sort(timestampComparator());
            } else {
                if(sliceId == null) {
                    allRequests.sort(messageIdComparator());
                } else {
                    allRequests.sort(messageIdComparator().thenComparing(sliceIdComparator()));
                }
            }
        }

        String ret = join2NormalStr(allRequests);
        ret = cutMaxLen(ret);
        return ret;
    }

    public static String cutMaxLen(String ret) {
        if (StringUtils.isNotEmpty(ret) && ret.length() > GlobalConf.MAX_CHECK_LEN) {
            ret = ret.substring(0, GlobalConf.MAX_CHECK_LEN);
        }
        return ret;
    }

    private static @NotNull Comparator<DefenseApiRequest> timestampComparator() {
        return (t1, t2) -> {
            Long id1 = t1.getTimestamp();
            Long id2 = t2.getTimestamp();
            if (id1 == null || id2 == null) {
                return 0;
            } else {
                return id1.compareTo(id2);
            }
        };
    }

    private static @NotNull Comparator<DefenseApiRequest> sliceIdComparator() {
        return (o1, o2) -> {
            Integer id1 = o1.getMessageInfo().getSliceId();
            Integer id2 = o2.getMessageInfo().getSliceId();
            if (id1 == null || id2 == null) {
                return 0;
            } else {
                return id1.compareTo(id2);
            }
        };
    }

    private static @NotNull Comparator<DefenseApiRequest> messageIdComparator() {
        return (o1, o2) -> {
            Integer id1 = o1.getMessageInfo().getMessageId();
            Integer id2 = o2.getMessageInfo().getMessageId();
            if (id1 == null || id2 == null) {
                return 0;
            } else {
                return id1.compareTo(id2);
            }
        };
    }

    private static String join2NormalStr(List<DefenseApiRequest> allRequests) {
        Integer curMessageId = null;
        StringBuilder builder = new StringBuilder();
        for (DefenseApiRequest req : allRequests) {
            Integer messageId = req.getMessageInfo().getMessageId();
            if (messageId == null) {
                if (StringUtils.isEmpty(builder.toString())) {
                    builder.append(req.getContent());
                } else {
                    builder.append("，").append(req.getContent());
                }
            } else {
                if (Objects.equals(curMessageId, messageId)) {
                    builder.append(req.getContent());
                } else {
                    if (StringUtils.isEmpty(builder.toString())) {
                        builder.append(req.getContent());
                    } else {
                        builder.append("，").append(req.getContent());
                    }
                    curMessageId = messageId;
                }
            }
        }
        return builder.toString();
    }
}
