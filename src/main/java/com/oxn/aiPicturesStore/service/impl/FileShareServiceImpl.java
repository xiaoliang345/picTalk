package com.oxn.aiPicturesStore.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxn.aiPicturesStore.config.CosClientConfig;
import com.oxn.aiPicturesStore.mapper.FileShareMapper;
import com.oxn.aiPicturesStore.model.entity.FileShare;
import com.oxn.aiPicturesStore.service.FileShareService;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;

@Service
public class FileShareServiceImpl extends ServiceImpl<FileShareMapper, FileShare> implements FileShareService {

    @Autowired
    private TransferManager transferManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Override
    public Upload uploadFile(File file, String key) {
        PutObjectRequest request = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return transferManager.upload(request);
    }
}