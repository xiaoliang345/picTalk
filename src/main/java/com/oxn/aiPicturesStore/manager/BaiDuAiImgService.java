package com.oxn.aiPicturesStore.manager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BaiDuAiImgService {

    @Value("${baidu.apiKey}")
    private String apiKey;

    @Value("${baidu.url}")
    private String url;

    @Value("${baidu.model}")
    private String model;

    @Value("${baidu.size}")
    private String size;

    @Value("${baidu.n}")
    private Integer n;

    @Value("${baidu.promptExtend}")
    private Boolean promptExtend;

    private OkHttpClient client;
    private ObjectMapper mapper;

    @PostConstruct
    public void init() {
        // 验证配置是否被正确加载
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("baidu.api-key 配置不能为空");
        }
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("baidu.url 配置不能为空");
        }
        
        this.client = new OkHttpClient.Builder()
                .readTimeout(300, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
    }

    // generateImage 方法
    public String generateImage(String prompt) throws IOException {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("请输入提示词");
        }

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", model);
        bodyMap.put("prompt", prompt);
        bodyMap.put("size", size);
        bodyMap.put("n", n);
        bodyMap.put("prompt_extend", promptExtend);

        String jsonBody = mapper.writeValueAsString(bodyMap);
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, JSON))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                throw new RuntimeException("HTTP " + response.code() + ": " + responseBody);
            }

            JsonNode root = mapper.readTree(responseBody);
            JsonNode data = root.path("data");
            if (data.isArray() && data.size() > 0) {
                JsonNode first = data.get(0);
                if (first.has("url")) {
                    return first.get("url").asText();
                }
            }
            throw new RuntimeException("无效的相应" + responseBody);
        }
    }

}