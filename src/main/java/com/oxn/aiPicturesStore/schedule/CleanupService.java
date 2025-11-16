package com.oxn.aiPicturesStore.schedule;

import com.oxn.aiPicturesStore.config.CosClientConfig;
import com.oxn.aiPicturesStore.mapper.FileShareMapper;
import com.oxn.aiPicturesStore.model.entity.FileShare;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.DeleteObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CleanupService {

    @Autowired
    private FileShareMapper fileShareMapper;

    @Autowired
    private COSClient cosClient;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 每天凌晨2点执行清理任务
     */
    @Scheduled(cron = "${cleanup.cron}")
    public void cleanupExpiredFiles() {
        log.info("开始执行过期文件清理任务");

        try {
            // 获取所有已过期的文件记录
            List<FileShare> expiredFiles = fileShareMapper.selectExpiredShares(LocalDateTime.now());
            log.info("找到 {} 个过期文件记录", expiredFiles.size());

            int successCount = 0;
            int failureCount = 0;

            // 删除每个过期文件在存储桶中的对象
            for (FileShare fileShare : expiredFiles) {
                try {
                    String folderKey = fileShare.getShareCode() + "/";
                    String fileKey = folderKey + fileShare.getFileName();
                    
                    // 直接删除文件对象（因为每个文件夹下只有一个文件）
                    try {
                        DeleteObjectRequest deleteRequest = new DeleteObjectRequest(cosClientConfig.getBucket(), fileKey);
                        cosClient.deleteObject(deleteRequest);
                        log.info("已删除存储桶中的对象: {}", fileKey);
                    } catch (CosClientException e) {
                        log.error("删除对象 {} 失败: {}", fileKey, e.getMessage(), e);
                    }
                    
                    // 从数据库中删除记录
                    fileShareMapper.deleteById(fileShare.getId());
                    log.info("已删除数据库记录，ID: {}, 提取码: {}", fileShare.getId(), fileShare.getShareCode());
                    successCount++;
                } catch (Exception e) {
                    log.error("处理过期文件时出错，文件ID: {}, 错误: {}", fileShare.getId(), e.getMessage(), e);
                    failureCount++;
                }
            }
            
            log.info("过期文件清理任务执行完成。成功: {}，失败: {}", successCount, failureCount);
        } catch (Exception e) {
            log.error("执行过期文件清理任务时出错: {}", e.getMessage(), e);
        }
    }
}