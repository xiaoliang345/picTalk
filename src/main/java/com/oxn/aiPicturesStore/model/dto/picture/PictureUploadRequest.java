package com.oxn.aiPicturesStore.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
  
    /**  
     * 图片 id（用于修改）  
     */  
    private Long id;

    /**
     * 根据地址上传
     */
    private String fileUrl;

    /**
     * 图片名称（用于批量抓取上传）
     */
    private String picName;


    private static final long serialVersionUID = 1L;
}
