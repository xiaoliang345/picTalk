package com.oxn.aiPicturesStore.model.vo;

import lombok.Data;

@Data
public class UploadVO {
    private String fileName;      // 存储的文件名
    private String code;  // 取件码
}