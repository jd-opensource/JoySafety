package com.jd.security.llmsec.service;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;



@Service
public class ScheduleWarmup {
    public volatile static List<Runnable> tasks = Lists.newLinkedList();
    private final static Object monitor = new Object();

    public static void appendTask(Runnable task) {
        synchronized (monitor) {
            tasks.add((task));
        }
    }

    private ExecutorService pool = Executors.newFixedThreadPool(3, new ThreadFactory() {
        private AtomicInteger cnt = new AtomicInteger();
        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread t = new Thread(r);
            t.setName("warm-up-" + cnt.getAndIncrement());
            return t;
        }
    });

    @Scheduled(initialDelay=5000, fixedRate = 60000)
    public void doWarmup() {
        synchronized (monitor) {
            for (Runnable r : tasks) {
                pool.submit(r);
            }
        }
    }
}
