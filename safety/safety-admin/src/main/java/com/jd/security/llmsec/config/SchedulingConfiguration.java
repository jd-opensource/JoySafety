package com.jd.security.llmsec.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ScheduledExecutorService;



@Configuration
public class SchedulingConfiguration {
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        taskScheduler.setThreadNamePrefix("TaskScheduler-");
        return taskScheduler;
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService(ThreadPoolTaskScheduler taskScheduler) {
        return taskScheduler.getScheduledExecutor();
    }
}
