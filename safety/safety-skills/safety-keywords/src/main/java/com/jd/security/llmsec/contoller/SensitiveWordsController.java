package com.jd.security.llmsec.contoller;

import com.jd.security.llmsec.core.rpc.KeywordApiService;
import com.jd.security.llmsec.core.rpc.KeywordApiService.KVRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/keyword")
public class SensitiveWordsController {

    @Autowired
    @Qualifier("KeywordApiServiceImpl")
    private KeywordApiService keywordApiService;


    @PostMapping("query")
    public Object query(@RequestBody KVRequest request) {
        return keywordApiService.invoke(request);
    }
}
