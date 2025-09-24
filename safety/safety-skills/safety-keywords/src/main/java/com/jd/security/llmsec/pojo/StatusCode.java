package com.jd.security.llmsec.pojo;

public enum StatusCode {
    SUCCESS(200, "Success"),
    INTERNAL_SERVER_ERROR(500, "Failure"),
    PARAM_LOSS(40002, "PARAM_LOSS");

    private final int code;
    private final String description;

    StatusCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}