package com.jd.security.llmsec.core.rpc;

import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponseX;

import java.util.List;



public interface DefenseApiService {
    ResponseMessage<List<DefenseApiResponseX>> invoke(String accessKey, DefenseApiRequest request);
}