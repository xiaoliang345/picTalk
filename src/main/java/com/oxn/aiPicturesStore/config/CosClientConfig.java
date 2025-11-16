package com.oxn.aiPicturesStore.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {  
  
    /**  
     * 域名  
     */  
    private String host;  
  
    /**  
     * secretId  
     */  
    private String secretId;  
  
    /**  
     * 密钥（注意不要泄露）  
     */  
    private String secretKey;  
  
    /**  
     * 区域  
     */  
    private String region;  
  
    /**  
     * 桶名  
     */  
    private String bucket;  
  
    @Bean
    public COSClient cosClient() {
        // 检查必要参数不为空
        if (secretId == null || secretKey == null || region == null) {
            throw new CosClientException("CosClient配置缺失，请检查配置文件中是否包含cos.client.secretId、cos.client.secretKey和cos.client.region");
        }
        
        // 初始化用户身份信息(secretId, secretKey)  
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224  
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 生成cos客户端  
        return new COSClient(cred, clientConfig);  
    }

    @Bean
    public TransferManager transferManager(COSClient cosClient) {
        TransferManager transferManager = new TransferManager(cosClient);
        // 可选：配置线程池、分块大小等
        TransferManagerConfiguration config = new TransferManagerConfiguration();
        config.setMultipartUploadThreshold(10 * 1024 * 1024); // 10MB 以上分片
        transferManager.setConfiguration(config);
        return transferManager;
    }
}