package com.oxn.aiPicturesStore.manager;

import com.oxn.aiPicturesStore.enums.TaskStatus;
import com.oxn.aiPicturesStore.model.dto.picture.AiImageTaskResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// AiTaskManager.java
@Component
public class AiTaskManager {

    private final Map<String, AiImageTaskResult> taskMap = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    public AiImageTaskResult getTask(String taskId) {
        return taskMap.get(taskId);
    }

    public String createTask(String description) {
        String taskId = "ai_img_" + counter.incrementAndGet();
        AiImageTaskResult result = new AiImageTaskResult();
        result.setTaskId(taskId);
        result.setStatus(TaskStatus.PENDING);
        result.setDescription(description);
        result.setCreateTime(System.currentTimeMillis());
        result.setUpdateTime(System.currentTimeMillis());
        taskMap.put(taskId, result);
        return taskId;
    }

    public void updateTask(String taskId, TaskStatus status, String resultUrl, String errorMsg) {
        AiImageTaskResult task = taskMap.get(taskId);
        if (task != null) {
            task.setStatus(status);
            task.setResultUrl(resultUrl);
            task.setErrorMsg(errorMsg);
            task.setUpdateTime(System.currentTimeMillis());
        }
    }
}