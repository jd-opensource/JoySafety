package com.jd.security.llmsec.contoller.rpc;

import com.jd.security.llmsec.core.rpc.KeywordApiService;



//todo 使用长连接rpc框架以减少IO和多线程消耗
//@Service
public class KeywordApiServiceHttpImpl implements KeywordApiService {
    @Override
    public KVResponse invoke(KVRequest request) {
        return null;
    }
}
