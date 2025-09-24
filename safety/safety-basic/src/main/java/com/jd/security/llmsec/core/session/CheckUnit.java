package com.jd.security.llmsec.core.session;



public enum CheckUnit {

    /**
     * 支持单个分片的检测
     */
    slice_single,

    /**
     * 支持多个分片的检测
     */
    slice_multi,

    /**
     * 支持单条消息的检测
     */
    message_single,

    /**
     * 支持多条消息的检测
     */
    message_multi
}
