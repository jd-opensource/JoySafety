package com.jd.security.llmsec.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;



@Service
public class AutoGc {
    private static Logger logger = LoggerFactory.getLogger(AutoGc.class);

    // 每天的 05:00 执行
    @Scheduled(cron = "0 0 5 * * ?")
    public void gc() {
        try {
            logger.info("准备定时触发gc");
            Random random = new Random();
            random = new Random(random.nextInt());

            // 半小时内随机开始执行
            TimeUnit.SECONDS.sleep(random.nextInt() % 1800);
            logger.info("开始触发gc");
            System.gc();
            logger.info("结束触发gc");
        } catch (Exception e) {
            logger.error("触发gc失败", e);
        }
    }
}
