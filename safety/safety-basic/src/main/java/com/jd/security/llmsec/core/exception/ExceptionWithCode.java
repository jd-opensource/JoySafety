package com.jd.security.llmsec.core.exception;

import lombok.Data;



@Data
public class ExceptionWithCode extends Exception{
    private int code;
    private String message;

    public ExceptionWithCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
