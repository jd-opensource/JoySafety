package com.jd.security.llmsec.core;

import lombok.Data;



@Data
public class ResponseMessage<T> {
    private static final int SUCCESS = ResponseCode.success.code;
    private static final int FAIL = ResponseCode.fail.code;

    private int code;
    private String message;
    private long cost;
    private T data;

    public ResponseMessage<T> cost(long cost) {
        this.cost = cost;
        return this;
    }

    public ResponseMessage() {
    }

    public ResponseMessage(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public boolean success() {
        return code == SUCCESS;
    }

    public static <T> ResponseMessage<T> success(String message, T data) {
        return new ResponseMessage<>(SUCCESS, message, data);
    }

    public static <T> ResponseMessage<T> success(T data) {
        return new ResponseMessage<>(SUCCESS, "success", data);
    }

    public static <T> ResponseMessage<T> fail(int code, String message, T data) {
        return new ResponseMessage<>(code, message, data);
    }

    public static <T> ResponseMessage<T> fail(String message, T data) {
        return fail(FAIL, message, data);
    }
}
