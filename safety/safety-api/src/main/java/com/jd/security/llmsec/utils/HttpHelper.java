package com.jd.security.llmsec.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Maps;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;



public class HttpHelper {
    private static Logger logger = LoggerFactory.getLogger(HttpHelper.class);

    private static final MediaType type = MediaType.get("application/json");

    private static OkHttpClient defaultClient = new OkHttpClient();
    public static <T> T post(String url, Map<String, String> headers, Object body, Class<T> responseType) throws IOException {
        return post(defaultClient, url, headers, body, responseType);
    }

    public static <T> T post(String url, Map<String, String> headers, Object body, TypeReference<T> responseType) throws IOException {
        return post(defaultClient, url, headers, body, responseType);
    }

    public static <T> T post(OkHttpClient client, String url, Map<String, String> headers, Object body, Class<T> responseType) throws IOException {
        String strBody = doPost(client, url, headers, body);
        if (strBody == null) {
            return null;
        } else {
            return JSON.parseObject(strBody, responseType);
        }
    }

    public static <T> T post(OkHttpClient client, String url, Map<String, String> headers, Object body, TypeReference<T> responseType) throws IOException {
        String strBody = doPost(client, url, headers, body);
        if (strBody == null) {
            return null;
        } else {
            return JSON.parseObject(strBody, responseType);
        }
    }

    private static @NotNull String doPost(OkHttpClient client, String url, Map<String, String> headers, Object body) throws IOException {
        long start = System.currentTimeMillis();
        Request.Builder reqBuilder = new Request.Builder();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                reqBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        RequestBody requestBody = RequestBody.create(type, JSON.toJSONString(body));
        Request request = reqBuilder.post(requestBody)
                .url(url)
                .build();

        String strBody = null;
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("接口调用失败(code异常), url={}, body={}, code={}", url, requestBody, response.code());
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                logger.error("接口调用失败(responseBody为null), url={}, body={}, code={}", url, requestBody, response.code());
            }

            strBody = responseBody.string();
            if (StringUtils.isEmpty(strBody)) {
                logger.error("接口调用失败(responseBody为空), url={}, body={}, code={}", url, requestBody, response.code());
            }
            logger.info("url={}, headers={}, req={}, response={}, cost={}ms", url, JSON.toJSONString(headers), JSON.toJSONString(body), strBody,
                    System.currentTimeMillis() - start);
        }
        return strBody;
    }

    public static Map<String, String> keepaliveHeader() {
        HashMap<String, String> headers = Maps.newHashMap();
        headers.put("Content-type", "application/json");
        headers.put("Connection", "Keep-Alive");
        // 20秒内/发达5000个请求后关闭连接
        headers.put("Keep-Alive", "timeout=20, max=5000");
        return headers;
    }
}
