package com.oxn.aiPicturesStore;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude =  {ShardingSphereAutoConfiguration.class})
@MapperScan("com.oxn.aiPicturesStore.mapper")
@EnableAspectJAutoProxy(exposeProxy = true )
public class AiPicturesStoreApplication {

    public static void main(String[] args) {
        // 加载 .env 文件
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .load();
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
        
        SpringApplication.run(AiPicturesStoreApplication.class, args);
    }

}
