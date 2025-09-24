package com.jd.security.llmsec.service.data;

import com.jd.security.llmsec.core.rpc.KeywordApiService;
import com.jd.security.llmsec.pojo.MatchType;

import java.util.concurrent.ExecutionException;


public interface SensitiveWordsManageService {

    MatchType.SensitiveWordsVO check(KeywordApiService.KVRequest vo) throws ExecutionException, InterruptedException;
}
