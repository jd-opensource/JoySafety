package com.jd.security.llmsec.core.conf.funcs;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.List;



@Data
public class KbSearchConf {
    private String url;
    private String cluster;
    private String collection;
    private Integer topK = 1;
    private Double threshold = 0.5;

    @Data
    public static class Req {
        private String cluster;
        private String collection;
        private String request_id;
        private String business_id;
        private String session_id;
        private Double threshold = 0.5;
        private Integer top_k = 1;
        private List<String> text_list;
    }

    @Data
    public static class Resp {
        private List<String> ids;
        private List<Double> scores;
        private List<String> text_list;
        private List<List<Double>> embeddings;
        private List<JSONObject> metadatas;
    }
}
