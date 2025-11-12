package com.oxn.aiPicturesStore.manager;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 使用通义千问 Image Edit 模型进行图片编辑的工具类
 */
@Service
public class ImageEditService {

    private static final Logger log = LoggerFactory.getLogger(ImageEditService.class);

    @Value("${dashscope.apiKey}")
    private String apiKey;

    @Value("${dashscope.apiUrl}")
    private String apiUrl;

    /**
     * 验证图片URL是否有效
     *
     * @param imageUrl 图片URL
     * @return 是否有效
     */
    private boolean isValidImageUrl(String imageUrl) {
        return imageUrl != null && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"));
    }

    /**
     * 使用通义千问 Image Edit 模型编辑图片
     *
     * @param originalImageUrl 原始图片的公网可访问 URL
     * @param editInstructions 编辑指令，描述你想要的图像效果
     * @return 编辑后图片的 URL
     * @throws RuntimeException 如果请求失败或返回错误
     */
    public String editImage(String originalImageUrl, String editInstructions) {
        // 参数校验
        if (StrUtil.isBlank(originalImageUrl)) {
            throw new IllegalArgumentException("原始图片 URL 不能为空");
        }
        if (StrUtil.isBlank(editInstructions)) {
            throw new IllegalArgumentException("编辑指令不能为空");
        }

        // 验证图片URL格式
        if (!isValidImageUrl(originalImageUrl)) {
            throw new IllegalArgumentException("无效的图片URL格式，请确保使用正确的HTTP或HTTPS图片链接");
        }

        // 构建 content 数组
        JSONArray contentArray = new JSONArray();
        contentArray.add(JSONUtil.createObj().set("image", originalImageUrl));
        contentArray.add(JSONUtil.createObj().set("text", editInstructions));

        // 构建 messages 数组
        JSONArray messagesArray = new JSONArray();
        messagesArray.add(JSONUtil.createObj()
                .set("role", "user")
                .set("content", contentArray));

        // 构建 input 对象
        JSONObject inputObj = JSONUtil.createObj()
                .set("messages", messagesArray);

        // 构建 parameters 对象
        JSONObject parametersObj = JSONUtil.createObj()
                .set("negative_prompt", " ") // 注意：原始示例中是空格，不是空字符串
                .set("watermark", false);

        // 构建最终请求体
        JSONObject requestBody = JSONUtil.createObj()
                .set("model", "qwen-image-edit")
                .set("input", inputObj)
                .set("parameters", parametersObj);

        log.info("正在发送图片编辑请求，请求体: {}", requestBody.toStringPretty());

        try (HttpResponse response = HttpRequest.post(apiUrl)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .body(requestBody.toString())
                .timeout(30000) // 30秒超时
                .execute()) {

            log.info("收到响应状态码: {}", response.getStatus());
            log.info("响应内容: {}", response.body());

            if (response.isOk()) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());

                // 检查是否包含 output 和 choices
                if (jsonResponse.containsKey("output")) {
                    JSONArray choices = jsonResponse.getJSONObject("output")
                            .getJSONArray("choices");

                    if (choices != null && choices.size() > 0) {
                        String imageUrl = choices.getJSONObject(0)
                                .getJSONObject("message")
                                .getJSONArray("content")
                                .getJSONObject(0)
                                .getStr("image");

                        if (StrUtil.isNotBlank(imageUrl)) {
                            log.info("✅ 图片编辑成功，结果图片 URL: {}", imageUrl);
                            return imageUrl;
                        } else {
                            throw new RuntimeException("API 返回成功，但未找到图片 URL");
                        }
                    } else {
                        throw new RuntimeException("API 返回中未找到 choices 数据");
                    }
                } else {
                    // 检查是否有具体的错误信息
                    if (jsonResponse.containsKey("code") && jsonResponse.containsKey("message")) {
                        String errorCode = jsonResponse.getStr("code");
                        String errorMessage = jsonResponse.getStr("message");
                        if ("InvalidParameter".equals(errorCode) && errorMessage.contains("download image failed")) {
                            throw new RuntimeException("图片编辑失败：无法下载原始图片，请确保图片URL可公开访问且有效。错误详情：" + errorMessage);
                        }
                        throw new RuntimeException("API 错误，代码：" + errorCode + "，消息：" + errorMessage);
                    }
                    throw new RuntimeException("API 响应中缺少 'output' 字段，响应: " + response.body());
                }
            } else {
                throw new RuntimeException("API 请求失败，状态码: " + response.getStatus() + "，响应: " + response.body());
            }
        } catch (RuntimeException e) {
            // 直接重新抛出运行时异常，避免包装
            throw e;
        } catch (Exception e) {
            log.error("调用通义千问图片编辑服务时发生异常", e);
            throw new RuntimeException("图片编辑请求失败: " + e.getMessage(), e);
        }
    }
}