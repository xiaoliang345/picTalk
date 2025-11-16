package com.oxn.aiPicturesStore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.oxn.aiPicturesStore.model.entity.FileShare;
import com.qcloud.cos.transfer.Upload;

import java.io.File;

public interface FileShareService extends IService<FileShare> {
    
    /**
     * 文件上传
     * @param file
     * @param key
     * @return
     */
    Upload uploadFile(File file, String key);
}