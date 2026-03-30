package com.oxn.aiPicturesStore;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = { ShardingSphereAutoConfiguration.class })
@MapperScan("com.oxn.aiPicturesStore.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class AiPicturesStoreApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(AiPicturesStoreApplication.class, args);
    }

    private static void loadDotenv() {
        try {
            Dotenv dotenv = new DotenvBuilder()
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        } catch (Exception ignored) {
        }
    }
}
