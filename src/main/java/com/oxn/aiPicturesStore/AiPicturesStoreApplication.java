package com.oxn.aiPicturesStore;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.oxn.aiPicturesStore.mapper")
@EnableAspectJAutoProxy(exposeProxy = true )
public class AiPicturesStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiPicturesStoreApplication.class, args);
    }

}
