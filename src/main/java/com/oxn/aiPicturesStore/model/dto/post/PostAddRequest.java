package com.oxn.aiPicturesStore.model.dto.post;


import lombok.Data;

/**
 * 
 * @TableName post
 */
@Data
public class PostAddRequest {

    /**
     * 发布者ID
     */
    private Long user_id;

    /**
     * 
     */
    private String title;

    /**
     * 
     */
    private String image_url;

    /**
     * 
     */
    private String description;


}