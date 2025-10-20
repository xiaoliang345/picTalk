package com.oxn.aiPicturesStore.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUpdateByAIRequest implements Serializable {

    /**
     * 要编辑的图片id
     */
    private Long id;

    /**
     * 修改的图片描述
     */
    private String description;
  
    private static final long serialVersionUID = 1L;  
}

