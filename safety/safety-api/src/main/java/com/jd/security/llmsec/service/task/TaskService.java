package com.jd.security.llmsec.service.task;

import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.api.defense.DefenseResultFetchRequest;

import java.util.List;



public interface TaskService {
    String TASK_QUEUE = "task:queue";
    String SESSION_NEW_MESG_PREFIX = "session:new:";
    int NEW_MESG_EXPIRE_SECONDS = 300;


    /*
    - 同一会话，对应一个待处理任务队列
    - 待处理会话队列使用zset
    - 每个结点统一会话队列里边取待处理的会话，分发给本地的线程
    - 每个处理线程使用分布式锁独占会话，顺序处理
     */
    void addTask(DefenseApiRequest request);


    List<DefenseApiResponse> fetchResult(String busineeName, String sessionId, DefenseResultFetchRequest.Type type);
}
