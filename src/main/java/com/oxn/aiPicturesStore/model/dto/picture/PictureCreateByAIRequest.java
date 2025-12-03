package com.oxn.aiPicturesStore.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureCreateByAIRequest implements Serializable {

    /**
     * 图片描述（AI 用来生成图片的提示词）
     */
    private String description;


    private static final long serialVersionUID = 1L;
}

