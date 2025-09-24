package com.jd.security.llmsec.service;



public interface RateLimitService {
    boolean hasLimit(String accessKey, String accessTarget);
}
