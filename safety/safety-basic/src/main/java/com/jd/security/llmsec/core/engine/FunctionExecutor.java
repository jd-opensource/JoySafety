package com.jd.security.llmsec.core.engine;

import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;
import com.jd.security.llmsec.core.session.SessionContext;
import lombok.Data;

import java.util.List;



public interface FunctionExecutor {
    @Data
    class Pair {
        private SessionContext context;
        private List<DefenseApiRequest> requests;
        private DefenseApiResponse response;

        public Pair() {
        }

        public Pair(SessionContext context, List<DefenseApiRequest> requests, DefenseApiResponse response) {
            this.context = context;
            this.requests = requests;
            this.response = response;
        }
    }

    /**
     * 调用防御API并返回响应
     * @param requests 防御API请求列表
     * @return 防御API响应
     */
    List<Pair> invoke(List<DefenseApiRequest> requests) throws ExceptionWithCode;


    /**
     * 调用防御API，并返回相应的API响应
     * @param request 防御API请求对象
     * @return 防御API响应对象
     */
    DefenseApiResponse invoke(DefenseApiRequest request) throws ExceptionWithCode;
}
