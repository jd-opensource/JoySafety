package com.jd.security.llmsec.service;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.jd.security.llmsec.core.BusinessConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



@Service
public class RateLimitServiceImpl implements RateLimitService {
    private Logger logger = LoggerFactory.getLogger(RateLimitServiceImpl.class);
    @Autowired
    @Qualifier("DynamicConfigService")
    private ConfigService configService;

    private volatile Map<String, RateLimiter> rateLimiterMap = Maps.newHashMap();
    private volatile Map<String, Long> waitMillisecondsMap = Maps.newHashMap();
    @PostConstruct
    private void init() {
        refresh();
    }

    @Scheduled(fixedRate = 60000)
    private void refresh() {
        try {
            List<BusinessConf> businessConfs = configService.businessConf();
            Map<String, RateLimiter> rateLimiterMap = Maps.newHashMap();
            Map<String, Long> waitMillisecondsMap = Maps.newHashMap();

            for (BusinessConf conf : businessConfs) {
                Integer totalLimit = conf.getQpsLimit();
                totalLimit = totalLimit != null && totalLimit > 0 ? totalLimit : 10;
                rateLimiterMap.put(conf.getAccessKey(), buildRateLimiter(totalLimit));
                waitMillisecondsMap.put(conf.getAccessKey(), 1000L / totalLimit + 10);

                Map<String, Integer> qpsLimits = conf.getQpsLimits();
                if (qpsLimits == null || qpsLimits.isEmpty()) {
                    continue;
                }

                for (Map.Entry<String, Integer> entry : qpsLimits.entrySet()) {
                    RateLimiter rateLimiter = buildRateLimiter(entry.getValue());
                    rateLimiterMap.put(key(conf.getAccessKey(), entry.getKey()), rateLimiter);
                }
            }

            this.rateLimiterMap = rateLimiterMap;
            this.waitMillisecondsMap = waitMillisecondsMap;
        } catch (Exception e) {
            logger.error("更新限流信息失败", e);
        }
    }

    private RateLimiter buildRateLimiter(Integer qps) {
        return RateLimiter.create(qps);
    }

    private String key(String accessKey, String key) {
        return accessKey + "$" + key;
    }

    @Override
    public boolean hasLimit(String accessKey, String accessTarget) {
        RateLimiter rateLimiter = rateLimiterMap.get(key(accessKey, accessTarget));
        rateLimiter = rateLimiter != null ? rateLimiter : rateLimiterMap.get(accessKey);
        if (rateLimiter == null) {
            return false;
        }
        if (rateLimiter.tryAcquire()) {
            return false;
        } else {
            return !rateLimiter.tryAcquire(1, waitMillisecondsMap.get(accessKey), TimeUnit.MILLISECONDS);
        }
    }
}
