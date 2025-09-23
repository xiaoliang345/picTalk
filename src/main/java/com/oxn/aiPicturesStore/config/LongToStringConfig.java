package com.oxn.aiPicturesStore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * 配置Long类型以String形式返回给前端
 * 解决大整数在前端JavaScript中精度丢失的问题
 */
@Configuration
public class LongToStringConfig {

    /**
     * 配置Jackson消息转换器，将Long类型序列化为String
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        // 创建ObjectMapper对象
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 创建SimpleModule，用于注册自定义序列化器
        SimpleModule simpleModule = new SimpleModule();
        
        // 为Long类型注册ToStringSerializer序列化器
        // 这样Long类型会被序列化为String类型
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        
        // 将SimpleModule注册到ObjectMapper
        objectMapper.registerModule(simpleModule);
        
        // 创建并返回MappingJackson2HttpMessageConverter
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
