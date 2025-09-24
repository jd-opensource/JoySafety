package com.jd.security.llmsec.service.task;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.jd.security.llmsec.config.GlobalConf;
import com.jd.security.llmsec.core.BusinessConf;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.api.defense.DefenseResultFetchRequest;
import com.jd.security.llmsec.core.engine.FunctionExecutor;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.CheckConf;
import com.jd.security.llmsec.core.session.History;
import com.jd.security.llmsec.core.session.ResponseMode;
import com.jd.security.llmsec.service.ConfigService;
import com.jd.security.llmsec.service.MonitorAlarmService;
import com.jd.security.llmsec.service.common.ThreadLocalService;
import com.jd.security.llmsec.service.storage.MessageQueueService;
import com.jd.security.llmsec.service.storage.StorageService;
import com.jd.security.llmsec.utils.ContentHelper;
import com.jd.security.llmsec.utils.ExceptionHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Pipeline;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.*;



@Service
public class TaskExecutorImpl implements TaskExecutor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ExecutorService pool;

    @Autowired
    private StorageService storageService;

    @Autowired
    private FunctionExecutor functionExecutor;

    @Autowired
    private ThreadLocalService threadLocalService;

    @PostConstruct
    private void init() {
        pool = new ThreadPoolExecutor(200, 200, 5, TimeUnit.MINUTES,
                new LinkedBlockingQueue<>(10),
                r -> {
                    Thread t = new Thread(r);
                    t.setUncaughtExceptionHandler((t1, e) -> logger.error("未捕获异常", e));
                    t.setName("task-executor-" + System.currentTimeMillis());
                    return t;
                }, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @PreDestroy
    private void destroy() {
        pool.shutdown();
    }

    private final int DEFAULT_BATCH_PROCESS_SIZE = 10;
    private final int DEFAULT_ADAPTIVE_PAUSE_MILLISECONDS = 20;
    private final int TASK_WAIT_LOOP = 1;

    private final int MAX_LOCK_SECONDS = 10;

    @Override
    public void execute(String sessionId) {
        pool.submit(new Runnable() {
            @Override
            public void run() {
                int batchSize = -1;
                CheckConf checkConf = null;
                for (int i = 0; i < TASK_WAIT_LOOP; i++) {
                    // 尝试多次获取锁
                    boolean lockSuccess = false;
                    try {
                        lockSuccess = storageService.lock(ContentHelper.lockKey(sessionId), MAX_LOCK_SECONDS);
                        if (lockSuccess) {
                            threadLocalService.sessionId.set(sessionId);
                            threadLocalService.histories.set(Lists.newArrayList());
                            threadLocalService.lastUserReq.remove();
                            logger.info("获取锁{}成功, id={}", ContentHelper.lockKey(sessionId), GlobalConf.lockId());
                        } else {
                            logger.debug("获取锁{}失败, id={}", ContentHelper.lockKey(sessionId), GlobalConf.lockId());
                        }
                    } catch (Exception e) {
                        logger.error("获取锁失败", e);
                        continue;
                    }
                    if (!lockSuccess) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(1 + System.currentTimeMillis() % 5);
                        } catch (InterruptedException e) {
                            logger.warn("waiting got interrupted", e);
                        }

                        continue;
                    }

                    try {
                        // 抢到锁时尽量把新的消息处理完
                        while (true) {
                            List<DefenseApiRequest> requests = null;
                            try {
                                requests = storageService.rpopN(ContentHelper.newTaskKey(sessionId), batchSize < 0 ? DEFAULT_BATCH_PROCESS_SIZE : batchSize, DefenseApiRequest.class);
                            } catch (Exception e) {
                                MonitorAlarmService.taskIncr("rpop_tasks_error");
                                logger.error("获取消息失败", e);
                                continue;
                            }
                            if (CollectionUtils.isEmpty(requests)) {
                                break;
                            }
                            logger.debug("获取异步识别任务成功，reqs={}", JSON.toJSONString(requests));

                            if (batchSize < 0) {
                                BusinessConf businessConf = configService.businessConf(requests.get(0));
                                checkConf = requests.get(0).fromRobot() ? businessConf.getRobotCheckConf() : businessConf.getUserCheckConf();
                                batchSize = checkConf.getCheckNum() <= 0 ? DEFAULT_BATCH_PROCESS_SIZE : checkConf.getCheckNum();
                            }

                            try {
                                // 续约，避免单个线程长期占用时时间无法估计的问题
                                storageService.execute(jedis -> jedis.expire(ContentHelper.lockKey(sessionId), MAX_LOCK_SECONDS));
                                doExecute(requests, sessionId);
                                MonitorAlarmService.taskIncr("exec_task_success", requests.size());
                            } catch (Exception e) {
                                MonitorAlarmService.taskIncr("exec_task_error", requests.size());
                                logger.error("异步处理请求失败, requests={}", JSON.toJSONString(requests), e);
                            }

                            // 随机sleep，降低cpu使用率，提高整体吞吐
                            try {
                                if (checkConf.getAdaptivePauseMilliseconds() <= 0) {
                                    TimeUnit.MILLISECONDS.sleep(DEFAULT_ADAPTIVE_PAUSE_MILLISECONDS + System.currentTimeMillis() % 10);
                                } else {
                                    TimeUnit.MILLISECONDS.sleep(checkConf.getAdaptivePauseMilliseconds() + System.currentTimeMillis() % 10);
                                }
                            } catch (InterruptedException e) {
                                logger.error("got interrupted when wait");
                            }
                        }
                    } finally {
                        try {
                            storageService.unlock(ContentHelper.lockKey(sessionId));
                            threadLocalService.sessionId.remove();
                            threadLocalService.histories.remove();
                            threadLocalService.lastUserReq.remove();
                            logger.info("释放锁={}成功, id={}", ContentHelper.lockKey(sessionId), GlobalConf.lockId());
                        } catch (Exception e) {
                            logger.error("解锁失败", e);
                            MonitorAlarmService.taskIncr("unlock_error");
                            try {
                                TimeUnit.SECONDS.sleep(2);
                            } catch (InterruptedException ex) {
                                logger.warn("waiting got interrupted", e);
                            }
                            try {
                                storageService.unlock(ContentHelper.lockKey(sessionId));
                                threadLocalService.sessionId.remove();
                                threadLocalService.histories.remove();
                                threadLocalService.lastUserReq.remove();
                                logger.info("释放锁={}成功, id={}", ContentHelper.lockKey(sessionId), GlobalConf.lockId());
                            } catch (Exception ex) {
                                logger.error("重试后解锁失败", e);
                                MonitorAlarmService.taskIncr("unlock_error");
                            }
                        }
                    }

                    return;
                }
                logger.debug("尝试{}次获取锁失败，sessionId={}", TASK_WAIT_LOOP, sessionId);
                MonitorAlarmService.taskIncr("task_lock_error");
            }
        });
    }

    @Autowired
    private MessageQueueService messageQueueService;

    @Autowired
    @Qualifier("DynamicConfigService")
    private ConfigService configService;

    private void doExecute(List<DefenseApiRequest> requests, String sessionId) throws ExceptionWithCode {
        List<FunctionExecutor.Pair> responses = functionExecutor.invoke(requests);
        for (FunctionExecutor.Pair pair : responses) {
            DefenseApiResponse response = pair.getResponse();
            ResponseMode mode = pair.getRequests().get(0).getResponseMode();
            if (response != null) {
                switch (mode) {
                    case sync:
                        break;
                    case free_taxi:
                    case http:
                    case mq:
                        boolean success = ExceptionHelper.doWithRetry(3, () -> {
                            String responseKey = ContentHelper.responseKey(sessionId);
                            storageService.execute(jedis -> {
                                Pipeline pipelined = jedis.pipelined();
                                pipelined.lpush(responseKey, JSON.toJSONString(response));
                                pipelined.expire(responseKey, RESP_EXPIRE_SECONDS);
                                return null;
                            });
                        });
                        if (!success) {
                            logger.error("写入识别结果到队列失败, sessionId={}, requests={}, response={}", sessionId, JSON.toJSONString(requests), JSON.toJSONString(response));
                        }
                        DefenseApiRequest request = requests.get(0);
                        messageQueueService.appendResponseData(pair.getContext(), request.getAccessKey(), request.getMessageInfo().getSessionId(), response);
                        break;
                }
            }
        }
    }

    private final int MAX_FETCH_NUM = 50;
    @Override
    public List<DefenseApiResponse> fetchResult(String businessName, String sessionId, DefenseResultFetchRequest.Type type) {
        String responseKey = ContentHelper.responseKey(ContentHelper.globalSessionKey(businessName, sessionId));
        try {
            switch (type) {
                case all:
                    List<String> jsons = storageService.rpopN(responseKey, MAX_FETCH_NUM, String.class);
                    List<DefenseApiResponse> responses = Lists.newArrayList();
                    for (String json : jsons) {
                        DefenseApiResponse response = History.responseFromJson(json);
                        responses.add(response);
                    }
                    return responses;
                case latest:
                    String json = storageService.rpop(responseKey, String.class);
                    if (json == null) {
                        return Lists.newArrayList();
                    } else {
                        DefenseApiResponse response = History.responseFromJson(json);
                        return Lists.newArrayList(response);
                    }
            }
        } catch (Exception e) {
            MonitorAlarmService.requestIncr(businessName, "fetch_result_error");
            logger.error("获取结果失败", e);
        }
        return Lists.newArrayList(); // should not be here
    }
}
