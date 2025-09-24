package com.jd.security.llmsec.service.execute.functions;

import com.alibaba.fastjson2.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.security.llmsec.core.ResponseCode;
import com.jd.security.llmsec.core.check.*;
import com.jd.security.llmsec.core.conf.FunctionConfig;
import com.jd.security.llmsec.core.conf.funcs.ParallelConf;
import com.jd.security.llmsec.core.conf.funcs.ParallelConf.GroovyConf;
import com.jd.security.llmsec.core.engine.AbstractFunction;
import com.jd.security.llmsec.core.engine.Function;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;
import com.jd.security.llmsec.core.util.GroovyHelper;
import com.jd.security.llmsec.service.MonitorAlarmService;
import com.jd.security.llmsec.utils.FunctionUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.jd.security.llmsec.service.execute.FunctionRegistry.buildFunction;



public class ParallelFunction extends AbstractFunction {
    private static Logger logger = LoggerFactory.getLogger(ParallelFunction.class);
    public ParallelFunction(FunctionConfig functionConf) throws Exception {
        super(functionConf);
    }

    private ParallelConf conf;
    private GroovyConf groovyConf;
    private ScriptEngine engine;
    private List<Function> functions;
    @Override
    protected void doInit(Map<String, Object> conf) throws Exception {
        ParallelConf thisConf = JSON.parseObject(JSON.toJSONString(conf), ParallelConf.class);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(thisConf.getFunctionConfs()), "functionConfs不能为空");
        Preconditions.checkArgument(thisConf.getReduceType() != null, "reduceType不能为空");
        this.conf = thisConf;
        switch (thisConf.getReduceType()) {
            case first_risk:
                break;
            case groovy_script:
                GroovyConf groovyConf = JSON.parseObject(JSON.toJSONString(thisConf.getExtra()), GroovyConf.class);
                Preconditions.checkArgument(groovyConf != null , "groovyConf不能为空");
                Preconditions.checkArgument(StringUtils.isNotEmpty(groovyConf.getScript()) , "script不能为空");
                this.groovyConf = groovyConf;
                this.engine = GroovyHelper.engin(groovyConf.getScript());
                break;
        }

        functions = Lists.newArrayList();
        for (FunctionConfig t : thisConf.getFunctionConfs()) {
            t = FunctionUtil.doMerge(t);
            Function function = buildFunction(t);
            functions.add(function);
        }
    }


    @Override
    public RiskCheckType type() {
        return RiskCheckType.parallel;
    }

    private static AtomicInteger cnt = new AtomicInteger(0);
    public static ExecutorService pool;
    static {
        // todo 根据实际情况调整
        pool = new ThreadPoolExecutor(200, 200, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>(1),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("function-parallel-" + cnt.incrementAndGet());
                    return t;
                }, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public RiskCheckResult doRun(SessionContext context) throws ExceptionWithCode {
        Map<String, RiskCheckResult> resultMap = Maps.newLinkedHashMap();
        List<Future<RiskCheckResult>> futures = Lists.newArrayList();
        long allStart = System.currentTimeMillis();
        List<Function> tempFuncs = Lists.newArrayList();
        for (Function func : functions) {
            if (func.conf().getTarget() == null || Objects.equals(func.conf().getTarget(), context.getCurReq().get(0).getMessageInfo().getFromRole())) {
                try {
                    Future<RiskCheckResult> checkFuture = pool.submit(() -> func.run(context));
                    futures.add(checkFuture);
                } catch (Exception e) {
                    logger.error("并发调用失败，function={}", func.name(), e);
                    MonitorAlarmService.parallelIncr(context.getBusinessConf().getName(), func.name(), "submit");
                }
                tempFuncs.add(func);
            }
        }

        if (CollectionUtils.isEmpty(futures)) {
            // 算是降级了
            return MixedCheck.noRisk();
        }

        long remainCost = this.conf().getTimeoutMilliseconds();
        for (int i = 0; i < futures.size(); i++) {
            Future<RiskCheckResult> f = futures.get(i);
            Function tempFunc = tempFuncs.get(i);
            long curTimeout = tempFunc.conf().getTimeoutMilliseconds();
            if (remainCost < 0) { // 已超时，如果能直接返回就返回
                curTimeout = 1;
            }

            long start = System.currentTimeMillis();
            try {
                RiskCheckResult riskCheckResult = f.get(curTimeout, TimeUnit.MILLISECONDS);
                resultMap.put(tempFunc.name(), riskCheckResult);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.error("并发调用失败，function={}, cost={}", tempFunc.name(), System.currentTimeMillis() - allStart, e);
                MonitorAlarmService.parallelIncr(context.getBusinessConf().getName(), tempFunc.name(), "invoke_error");
            }
            remainCost -= (System.currentTimeMillis() - start);
        }

        ParallelCheckResult result = new ParallelCheckResult();
        result.setResultMap(resultMap);

        switch (conf.getReduceType()) {
            case no_reduce:
                break;
            case first_risk:
                Optional<RiskCheckResult> firstRisk = resultMap.values().stream().filter(r -> r.hasRisk()).findFirst();
                if (firstRisk.isPresent()) {
                    return firstRisk.get();
                } else {
                    return MixedCheck.noRisk();
                }
            case groovy_script:
                try {
                    RiskCheckResult risk;
                    synchronized (engine) {
                        Bindings bindings = engine.createBindings();
                        bindings.put("ctx", context);
                        bindings.put("conf", this.conf);
                        bindings.put("retMap", resultMap);
                        risk = (RiskCheckResult) engine.eval(groovyConf.getScript(), bindings);
                    }
                    if (risk == null) {
                        return MixedCheck.noRisk();
                    } else {
                        return risk;
                    }
                } catch (ScriptException e) {
                    logger.error("执行脚本失败, ctx={}, resultMap={}", JSON.toJSONString(context), JSON.toJSONString(resultMap), e);
                    MonitorAlarmService.parallelIncr(context.getBusinessConf().getName(), this.name(), "groovy_reduce_error");
                    throw new ExceptionWithCode(ResponseCode.inner_error.code, e.getMessage());
                }
        }

        // should not be here
        return result;
    }
}
