package com.oxn.aiPicturesStore.manager;

import com.qcloud.cos.transfer.TransferProgress;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// UploadProgressService.java
@Component
public class UploadProgressManage {
    // 使用延迟初始化 + synchronized 确保线程安全
    private final Map<String, TransferProgress> progressMap = new ConcurrentHashMap<>();

    public void updateProgress(String uploadId, TransferProgress progress) {
        progressMap.put(uploadId, progress);
    }

    public TransferProgress getProgress(String uploadId) {
        return progressMap.get(uploadId);
    }
}