package com.oxn.aiPicturesStore.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadByBatchRequest implements Serializable {
  
    /**  
     * 搜索关键词
     */  
    private String searchText;

    /**
     * 抓取数量
     */
    private int count;

    /**
     * 名称前缀
     */
    private String namePrefix;
    
    /**
     * 是否只抓取高质量图片
     */
    private Boolean highQualityOnly = true;
    
    /**
     * 最小图片大小（字节），默认50KB
     */
    private Long minImageSize = 50 * 1024L;
    
    /**
     * 是否尝试获取原图
     */
    private Boolean tryOriginalSize = true;

    private static final long serialVersionUID = 1L;  
}
