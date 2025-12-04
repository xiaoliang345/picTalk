package com.oxn.aiPicturesStore.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxn.aiPicturesStore.manager.upload.FilePictureUpload;
import com.oxn.aiPicturesStore.manager.upload.PictureUploadTemplate;
import com.oxn.aiPicturesStore.mapper.PictureMapper;
import com.oxn.aiPicturesStore.mapper.PostImageMapper;
import com.oxn.aiPicturesStore.model.dto.file.UploadPictureResult;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.PostImage;
import com.oxn.aiPicturesStore.service.PostImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostImageServiceImpl extends ServiceImpl<PostImageMapper, PostImage> implements PostImageService {

    @Autowired
    private FilePictureUpload filePictureUpload;

    @Override
    public Map<String,String> uploadPicture(MultipartFile multipartFile, Long postId) {

        PictureUploadTemplate uploadTemplate = filePictureUpload;
        UploadPictureResult uploadPictureResult = uploadTemplate.uploadPicture(multipartFile, "post");
        HashMap<String, String> pictureUrlMap = new HashMap<>();
        pictureUrlMap.put("url", uploadPictureResult.getUrl());
        pictureUrlMap.put("thumbnailUrl", uploadPictureResult.getThumbnailUrl());
        pictureUrlMap.put("previewUrl", uploadPictureResult.getPreviewUrl());
        return pictureUrlMap;
    }
}
