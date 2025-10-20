package com.oxn.aiPicturesStore.model.dto.picture;

import com.oxn.aiPicturesStore.enums.TaskStatus;
import lombok.Data;

// AiImageTaskResult.java

@Data
public class AiImageTaskResult {
    private String taskId;
    private TaskStatus status;
    private String resultUrl;  // 成功时返回图片 URL
    private String errorMsg;   // 失败时返回错误信息
    private String description;
    private Long createTime;
    private Long updateTime;

}