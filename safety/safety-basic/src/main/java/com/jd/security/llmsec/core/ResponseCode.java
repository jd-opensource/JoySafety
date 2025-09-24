package com.jd.security.llmsec.core;



public enum ResponseCode {
    success(0, "请求成功"),
    fail(1, "未知异常"),
    not_allowed(400, "访问未授权"), // 兼容旧接口
    nonexist_resource(404, "请求资源在服务器上不存在"), // 兼容旧接口

    param_invalid(40001, "参数非法"),
    token_error(40002, "token错误/业务不存在"),
    sign_expire(40003, "签名过期"),
    sign_not_matched(40004, "签名值错误"),

    inner_error(500, "服务器内部异常"), // 兼容旧接口
    cannot_response_for_now(503, "服务器暂时无法响应"), // 兼容旧接口


    rate_limited(50001, "触发限流"),
    exec_timeout(50002, "内部执行超时")
    ;


    public final int code;
    public final String description;
    ResponseCode(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
