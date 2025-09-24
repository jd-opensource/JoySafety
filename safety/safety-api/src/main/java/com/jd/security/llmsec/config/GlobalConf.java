package com.jd.security.llmsec.config;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jd.security.llmsec.core.session.CheckConf;
import com.jd.security.llmsec.core.session.SessionContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;



@Configuration
public class GlobalConf {
    private Logger logger = LoggerFactory.getLogger(GlobalConf.class);

    @Value("${global.conf.cache_prefix:}")
    private String cachePrefix;

//    @Value("${spring.profiles.active}")
//    private String profile;
//    public static String confBaseDir;

    @Value("${trade_sec.ignore_class_name}")
    private String ignoreClassName;
    private List<String> ignoreClassNames = Lists.newArrayList();

    @Value("${context.history.ignore_bizs}")
    private String historyIgnoreBiz;
    private List<String> historyIgnoreBizs = Lists.newArrayList();

    public static String CACHE_PREFIX;
    public static String localIp = "un";

    public final static int MAX_CHECK_LEN = 7000;

    @PostConstruct
    private void init() {
        CACHE_PREFIX = cachePrefix;

        try {
//            confBaseDir = "conf_" + profile;

            // 获取本地主机
            InetAddress localHost = InetAddress.getLocalHost();

            // 获取IP地址
            String ipAddress = localHost.getHostAddress();

            // 打印IP地址
            logger.info("本地服务器IP地址: " + ipAddress);
            localIp = ipAddress;
        } catch (UnknownHostException e) {
            logger.info("无法获取本地IP地址", e);
        }

        initOpenaiConf();


        tradeSecConf();

        if (StringUtils.isNotEmpty(historyIgnoreBiz)) {
            List<String> temp = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(historyIgnoreBiz);
            if (CollectionUtils.isNotEmpty(temp)) {
                this.historyIgnoreBizs = temp;
            }
        }
    }

    private void tradeSecConf() {
        if (StringUtils.isNotEmpty(ignoreClassName)) {
            List<String> temp = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(ignoreClassName);
            if (CollectionUtils.isNotEmpty(temp)) {
                this.ignoreClassNames = temp;
            }
        }
    }

//    public String confBaseDir() {
//        return confBaseDir;
//    }

    public static String lockId() {
        return Joiner.on("/").join(GlobalConf.localIp, Thread.currentThread().getName(), System.currentTimeMillis());
    }

    @Value("${openai.stream.business}")
    private String openaiStreamBusiness;
    private Set<String> streamBusiness = Sets.newHashSet();

    @Value("${openai.request.business}")
    private String openaiRequestBusiness;
    private Set<String> openaiRequestBizs = Sets.newHashSet();
    private void initOpenaiConf() {
        if (StringUtils.isNotEmpty(openaiStreamBusiness)) {
            List<String> business = Splitter.on(",").splitToList(openaiStreamBusiness);
            if (CollectionUtils.isNotEmpty(business)) {
                streamBusiness.addAll(business);
            }
        }

        if (StringUtils.isNotEmpty(openaiRequestBusiness)) {
            List<String> business = Splitter.on(",").splitToList(openaiRequestBusiness);
            if (CollectionUtils.isNotEmpty(business)) {
                openaiRequestBizs.addAll(business);
            }
        }
    }

    public Set<String> streamBusiness() {
        return streamBusiness;
    }

    public Set<String> openaiRequestBizs() {
        return openaiRequestBizs;
    }

    public boolean ignore(String className) {
        return this.ignoreClassNames.contains(className);
    }

    public boolean historyIgnore(String businessName, SessionContext context) {
        CheckConf userCheckConf = context.getCurReq().get(0).fromUser() ? context.getBusinessConf().getUserCheckConf() : context.getBusinessConf().getRobotCheckConf();
        if (userCheckConf.getCheckNum() < 2) {
            return true;
        }

        return this.historyIgnoreBizs.contains(businessName);
    }
}
