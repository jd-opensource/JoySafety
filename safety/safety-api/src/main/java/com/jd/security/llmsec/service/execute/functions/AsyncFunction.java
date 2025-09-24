package com.jd.security.llmsec.service.execute.functions;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.check.AsyncCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckResult;
import com.jd.security.llmsec.core.check.RiskCheckType;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.engine.AbstractFunction;
import com.jd.security.llmsec.core.engine.Function;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;
import com.jd.security.llmsec.service.BeanBridge;
import com.jd.security.llmsec.service.execute.FunctionRegistry;
import com.jd.security.llmsec.utils.ContextHelper;
import com.jd.security.llmsec.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;



public class AsyncFunction extends AbstractFunction {
    private static Logger logger = LoggerFactory.getLogger(AsyncFunction.class);
    public AsyncFunction(FunctionConfig functionConf) throws Exception {
        super(functionConf);
    }

    private FunctionConfig realFuncConf;
    private Function function;
    @Override
    protected void doInit(Map<String, Object> map) throws Exception {
        this.realFuncConf = JsonUtils.obj2obj(map, FunctionConfig.class);
        Preconditions.checkNotNull(this.realFuncConf, "real conf为空");
        this.function = FunctionRegistry.buildFunction(this.realFuncConf);
    }

    private static AtomicInteger cnt = new AtomicInteger(0);
    public static ExecutorService pool;
    static {
        // todo 根据实际情况调整
        pool = new ThreadPoolExecutor(20, 20, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>(50),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("function-async-" + cnt.incrementAndGet());
                    return t;
                }, new ThreadPoolExecutor.DiscardOldestPolicy());
    }
    @Override
    protected RiskCheckResult doRun(SessionContext sessionContext) throws ExceptionWithCode {
        pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    SessionContext ctxCopy = new SessionContext();
                    BeanUtils.copyProperties(sessionContext, ctxCopy);
                    ctxCopy.setMiddleResults(Maps.newHashMap());
                    ctxCopy.setExecuteStartTime(System.currentTimeMillis());
                    ctxCopy.setEndReason(null);
                    RiskCheckResult funcResult = function.run(ctxCopy);
                    if (funcResult == null) {
                        logger.warn("异步执行检测失败, ctx={}", JSON.toJSONString(sessionContext));
                        return;
                    }

                    DefenseApiRequest firstReq = sessionContext.getCurReq().get(0);

                    AsyncCheckResult asyncCheckResult = new AsyncCheckResult();
                    asyncCheckResult.setRiskCode(funcResult.getRiskCode());
                    asyncCheckResult.setRiskMessage(funcResult.getRiskMessage());
                    asyncCheckResult.setResult(funcResult);

                    ctxCopy.setCurResult(asyncCheckResult);

                    long endTime = System.currentTimeMillis();
                    ctxCopy.setExecuteEndTime(endTime);
                    ctxCopy.setExecuteCost(endTime - ctxCopy.getExecuteStartTime());
                    ctxCopy.setRecvExecuteCost(endTime - firstReq.getTimestamp());

                    DefenseApiResponse defenseApiResponse = ContextHelper.buildResp(ctxCopy.getCurReq(), asyncCheckResult, ctxCopy);
                    BeanBridge.gMessageQueueService.appendResponseData(ctxCopy, firstReq.getAccessKey(), firstReq.getMessageInfo().getSessionId(), defenseApiResponse);
                    ContextHelper.fullLog(ctxCopy);

                } catch (ExceptionWithCode e) {
                    logger.error("异步执行检测失败, ctx={}", JSON.toJSONString(sessionContext), e);
                }
            }
        });
        return sessionContext.getCurResult();
    }

    @Override
    public RiskCheckType type() {
        return RiskCheckType.async;
    }
}
