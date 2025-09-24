package com.jd.security.llmsec.demo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.google.common.collect.Maps;
import com.jd.security.llmsec.core.BusinessConf;
import com.jd.security.llmsec.core.ResponseMessage;
import com.jd.security.llmsec.core.api.defense.DefenseApiRequest;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponse;
import com.jd.security.llmsec.core.api.defense.DefenseApiResponseX;
import com.jd.security.llmsec.core.session.BusinessType;
import com.jd.security.llmsec.core.session.MessageInfo;
import com.jd.security.llmsec.core.session.ResponseMode;
import com.jd.security.llmsec.core.session.Role;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;


public class DefenseApiDemo {
    // todo 使用时替换成真实值
    public static String accessKey = "test";
    public static String secretKey = "123456";
    public static void main(String[] args) {
        if (args.length == 2) {
            accessKey = args[0];
            secretKey = args[1];
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("请输入：");
            String input = scanner.nextLine();
            doRequest(input);

        }
    }

    private static void doRequest(String input) {
        DefenseApiRequest request = buildReq(input);

        RequestBody body = RequestBody.create(JSON.toJSONString(request), MediaType.get("application/json"));
        Request httpReq = new Request.Builder()
                // todo 使用时替换成真实值
                .url("http://safety-api:8007/llmsec/api/defense/v2/" + accessKey)
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(httpReq).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String str = responseBody.string();
                ResponseMessage<List<DefenseApiResponseX>> responseMessage = JSON.parseObject(str, new TypeReference<ResponseMessage<List<DefenseApiResponseX>>>() {});
                if (!responseMessage.success()) {
                    System.out.println("请求失败, error=" + responseMessage.getMessage());
                    return;
                }

                handleResponse(responseMessage);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void handleResponse(ResponseMessage<List<DefenseApiResponseX>> responseMessage) {
        DefenseApiResponse defenseApiResponse = responseMessage.getData().get(0).toResponse();
        if (defenseApiResponse.hasRisk()) {
            System.out.println("有风险：" + defenseApiResponse.getRiskMessage());
            System.out.println(defenseApiResponse.getRiskCheckResult());
        } else {
            System.out.println("无风险");
        }
    }

    public static @NotNull DefenseApiRequest buildReq(String input) {
        BusinessConf authConf = new BusinessConf();
        authConf.setAccessKey(accessKey);
        authConf.setSecretKey(secretKey);

        MessageInfo messagInfo = MessageInfo.messageInfoBuilder()
                .sessionId(String.valueOf(System.currentTimeMillis()))
                .messageId(1)
                .fromRole(Role.user) // 用户输入
                .fromId("user1")
                .toRole(Role.user)
                .toId("user123456")
                .ext(new JSONObject()).build();

        DefenseApiRequest request = DefenseApiRequest.builder()
                .timestamp(System.currentTimeMillis())
                .requestId(UUID.randomUUID().toString())
                .accessKey(accessKey)
                .accessTarget("defenseV2")
                .messageInfo(messagInfo)
                .content(input)
                .businessType(BusinessType.toC)
                .responseMode(ResponseMode.sync)
                .build();

        request.sign(authConf);
        return request;
    }
}
