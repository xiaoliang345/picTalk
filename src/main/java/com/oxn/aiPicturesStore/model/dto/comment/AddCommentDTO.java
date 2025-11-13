package com.oxn.aiPicturesStore.model.dto.comment;

import lombok.Data;

@Data
public class AddCommentDTO {
    private Long postId;
    private Long parentId; // 可为空
    private Long replyToUserId; // 可为空
    private String content;
}