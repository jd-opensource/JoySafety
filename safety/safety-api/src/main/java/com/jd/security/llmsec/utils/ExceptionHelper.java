package com.jd.security.llmsec.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ExceptionHelper {
    private static Logger logger = LoggerFactory.getLogger(ExceptionHelper.class);
    public interface Call {
        void run() throws Exception;
    }

    /**
     * 通过重试机制执行指定的调用
     * @param retries 重试次数
     * @param call 调用对象
     * @return 最终执行成功则返回true，否则返回false
     */
    public static boolean doWithRetry(int retries, Call call) {
        for (int i = 0; i < retries; i++) {
            try {
                call.run();
                return true;
            } catch (Exception e) {
                logger.error("执行第{}/{}次, 执行出错", i+1, retries, e);
            }
        }
        return false;
    }
}
