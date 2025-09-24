package com.jd.security.llmsec.service;

import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.api.defense.DefenseResultFetchRequest;
import com.jd.security.llmsec.core.exception.ExceptionWithCode;

import java.util.List;



public interface DefenseService {
    /**
     * 检查防御 API 请求的参数是否有效
     * @param request 防御 API 请求对象
     * @return 如果参数有效则返回 true，否则返回 false
     * @throws Exception 如果参数检查过程中发生异常，则抛出异常
     */
    void checkParams(DefenseApiRequest request) throws Exception;

    boolean rateLimited(DefenseApiRequest request) throws ExceptionWithCode;

    /**
     * 验证防御API请求是否有效
     * @param request 防御API请求对象
     * @return boolean 验证结果，true表示有效，false表示无效
     * @throws ExceptionWithCode 当验证过程中发生异常时抛出
     */
    void verify(DefenseApiRequest request) throws ExceptionWithCode;


    /**
     * 验证防御结果获取请求是否有效
     * @param request 防御结果获取请求对象
     * @return 如果请求有效则返回true，否则返回false
     * @throws Exception 可能抛出异常
     */
    void verify(DefenseResultFetchRequest request) throws Exception;

    void preProcess(DefenseApiRequest request) throws ExceptionWithCode;

    /**
     * 提交防御API请求，主要用于异步处理的情况
     *
     * @param request 防御API请求对象
     */
    void submit(DefenseApiRequest request);

    /**
     * 处理防御API请求并返回防御API响应列表，主要用于同步处理的情况
     * @param request 防御API请求
     * @return 响应的防御API响应列表
     * @throws ExceptionWithCode 可能会抛出异常
     */
    List<DefenseApiResponse> process(DefenseApiRequest request) throws ExceptionWithCode;

    /**
     * 从服务器获取防御结果响应列表
     * @param request 防御结果获取请求对象
     * @return 防御结果响应列表
     * @throws ExceptionWithCode 异常情况，带有错误代码
     */
    List<DefenseApiResponse> fetchResult(DefenseResultFetchRequest request) throws ExceptionWithCode;

    /**
     * 获取防御响应结果列表
     * @param sessionId 会话ID
     * @return 防御响应结果列表
     */
    List<DefenseApiResponse> fetchResult(String businessName, String sessionId, DefenseResultFetchRequest.Type type);
}
