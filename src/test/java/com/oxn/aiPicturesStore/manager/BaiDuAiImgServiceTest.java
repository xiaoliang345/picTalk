package com.oxn.aiPicturesStore.manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BaiDuAiImgServiceTest {

    @Autowired
    private BaiDuAiImgService baiDuAiImgService;

    @Test
    void generateImage() throws IOException {
        
        String s = baiDuAiImgService.generateImage("凹版印刷，彩色图片，" +
                "精致的线条细致线条刻画细节，0.01毫米线条，绿黄蓝关系，以点线表现明暗关系，大师级排线，细致的细节，巨大的弯月从浓厚的云层中露出来，画而底部是遥远的地平线，高清画质。");
        System.out.println(s);
        assertNotNull(s, "图片 URL 应该不为 null");
        assertTrue(s.startsWith("http"), "图片 URL 应该以 http 开头");
    }
}