package com.jd.security.llmsec.service.task;

import com.alibaba.fastjson.JSON;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.api.defense.DefenseResultFetchRequest;
import com.jd.security.llmsec.service.MonitorAlarmService;
import com.jd.security.llmsec.service.storage.StorageService;
import com.jd.security.llmsec.utils.ContentHelper;
import com.jd.security.llmsec.utils.ExceptionHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Pipeline;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.TimeUnit;



@Service
public class TaskServiceImpl implements TaskService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String DISPATCHER_NAME = "task-local-dispatcher";

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private StorageService storageService;
    private volatile boolean running = false;

    @PostConstruct
    private void init() {
        running = true;
        Thread taskDispacher = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        String sessionId = storageService.rpop(ContentHelper.taskQueueKey(), String.class);
                        if (StringUtils.isEmpty(sessionId)) {
                            TimeUnit.MILLISECONDS.sleep(10);
                            continue;
                        }
                        logger.debug("分发会话任务, sessionId={}", sessionId);
                        taskExecutor.execute(sessionId);

                    } catch (Exception e) {
                        logger.error("任务分发异常", e);
                    }
                }
                logger.info("任务分发线程退出");
            }
        });
        taskDispacher.setName(DISPATCHER_NAME);
        taskDispacher.start();
    }

    @PreDestroy
    private void destroy() {
        running = false;
    }


    @Override
    public void addTask(DefenseApiRequest request) {
        // 避免相同业务sessionId冲突
        String sessionId = ContentHelper.globalSessionKey(request.getAccessKey(),  request.getMessageInfo().getSessionId());
        String newMsgKey = ContentHelper.newTaskKey(sessionId);
        boolean success = ExceptionHelper.doWithRetry(3, () -> storageService.execute(jedis -> {
            Pipeline pipelined = jedis.pipelined();
            pipelined.lpush(ContentHelper.taskQueueKey(), sessionId);
            pipelined.lpush(newMsgKey, JSON.toJSONString(request));
            pipelined.expire(newMsgKey, NEW_MESG_EXPIRE_SECONDS);
            pipelined.sync();
            return null;
        }));

        if (!success) {
            logger.error("添加异步识别任务失败，req={}", JSON.toJSONString(request));
            MonitorAlarmService.taskIncr("add_task_error");
        } else {
            MonitorAlarmService.taskIncr("add_task_success");
            logger.info("添加异步识别任务成功，req={}", JSON.toJSONString(request));
        }
    }

    @Override
    public List<DefenseApiResponse> fetchResult(String busineeName, String sessionId, DefenseResultFetchRequest.Type type) {
        return taskExecutor.fetchResult(busineeName, sessionId, type);
    }
}
