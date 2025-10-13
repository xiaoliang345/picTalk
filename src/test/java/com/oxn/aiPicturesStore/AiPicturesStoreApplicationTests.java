package com.oxn.aiPicturesStore;

import com.oxn.aiPicturesStore.enums.SpaceLevelEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;

@SpringBootTest
class AiPicturesStoreApplicationTests {

    @Test
    void contextLoads() {
        SpaceLevelEnum[] values = SpaceLevelEnum.values();
        for(int i=0;i<values.length;i++){
            System.out.println(values[i]);
        }
    }

}
