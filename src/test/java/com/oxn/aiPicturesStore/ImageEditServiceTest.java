package com.oxn.aiPicturesStore;

import com.oxn.aiPicturesStore.manager.ImageEditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageEditServiceTest {

    @Autowired
    private ImageEditService imageEditService;

    @Test
    void testEditImage_ShouldReturnImageUrl() {
        // 给定：原始图片 URL 和编辑指令
        String originalImageUrl = "https://help-static-aliyun-doc.aliyuncs.com/file-manage-files/zh-CN/20250925/fpakfo/image36.webp";
        String editInstructions = "生成一张符合深度图的图像，遵循以下描述：一辆红色的破旧的自行车停在一条泥泞的小路上，背景是茂密的原始森林";

        // 当：调用编辑方法
        String resultImageUrl = null;
        Exception thrownException = null;

        try {
            resultImageUrl = imageEditService.editImage(originalImageUrl, editInstructions);
            System.out.println("接口响应");
            System.out.println(resultImageUrl);
        } catch (Exception e) {
            thrownException = e;
        }

        // 那么：应成功返回图片 URL，无异常
        assertNull(thrownException, "调用 editImage 不应抛出异常");
        assertNotNull(resultImageUrl, "返回的图片 URL 不应为 null");
        assertTrue(resultImageUrl.startsWith("http"), "返回的应是一个有效的 HTTP/HTTPS 链接");
        System.out.println("✅ 编辑后的图片 URL: " + resultImageUrl);
    }

    @Test
    void testEditImage_WithInvalidApiKey_ShouldThrowException() {
        // 假设你有一个方式可以临时设置错误的 key（例如通过配置文件 profile）
        // 此测试用于验证错误处理（可选）
        // 实际中建议使用 Mock（见下方说明）
    }
}