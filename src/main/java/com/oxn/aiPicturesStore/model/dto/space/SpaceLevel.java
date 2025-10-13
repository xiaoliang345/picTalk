package com.oxn.aiPicturesStore.model.dto.space;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpaceLevel {

    /**
     * 空间类型
     */
    private  String text;

    /**
     * 类型值
     */
    private  Integer value;

    /**
     * 空间最大存储数
     */
    private  Integer maxCount;

    /**
     * 空间最大存储空间
     */
    private  long maxSize;


}
