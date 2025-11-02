package com.oxn.aiPicturesStore.manager;

import com.oxn.aiPicturesStore.enums.TaskStatus;
import com.oxn.aiPicturesStore.model.dto.picture.AiImageTaskResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// AiTaskManager.java
@Component
public class AiTaskManager {

    private final Map<String, AiImageTaskResult> taskMap = new ConcurrentHashMap<>();

    public AiImageTaskResult getTask(String taskId) {
        return taskMap.get(taskId);
    }

    public String createTask(String description) {
        String taskId = "ai_img_" + UUID.randomUUID().toString().replace("-", "");
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