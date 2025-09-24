package com.jd.security.llmsec.service;

import org.springframework.stereotype.Service;



@Service
public class MonitorAlarmService {
    // todo 增加监控方案
    static class Counter {
    }

    static class MetricPlan<T> {
    }

    static class MonitorContext {
        public static MetricPlan<Counter> getMetricRegistry() {
            return new MetricPlan<>();
        }
    }

    // 配置加载
    private static final MetricPlan<Counter> basicPlan = MonitorContext.getMetricRegistry();

    public static void basicIncr(String type) {
    }

    private static final MetricPlan<Counter> parallelPlan = MonitorContext.getMetricRegistry();

    // 并行function执行异常计数
    public static void parallelIncr(String businessName, String functionName, String reason) {
    }

    // function执行异常计数
    private static final MetricPlan<Counter> functionPlan = MonitorContext.getMetricRegistry();

    public static void functionIncr(String businessName, String functionName, String reason) {
    }

    private static final MetricPlan<Counter> requestPlan = MonitorContext.getMetricRegistry();

    public static void requestIncr(String biz, String type) {
    }


    // 接口调用计数
    private static final MetricPlan<Counter> apiPlan = MonitorContext.getMetricRegistry();

    public static void apiPlanIncr(String version, String businessName, String type) {
    }

    // 写jdq、redis、添加异步任务
    private static final MetricPlan<Counter> taskPlan = MonitorContext.getMetricRegistry();

    public static void taskIncr(String type) {
        taskIncr(type, 1);
    }

    public static void taskIncr(String type, int increment) {
    }

    private static final MetricPlan<Counter> riskPlan = MonitorContext.getMetricRegistry();

    public static void riskIncr(String biz, String type) {
    }
}
