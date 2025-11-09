package com.oxn.aiPicturesStore.model.dto.post;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UpdatePostDTO {
    private Long id;
    private String title;
    private String content;
    private List<Map<String, String>> imageUrls;
}