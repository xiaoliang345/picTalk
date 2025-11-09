package com.oxn.aiPicturesStore.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface PostImageService {

    /**
     * 多图片上传
     */
    Map<String,String> uploadPicture(MultipartFile multipartFile, Long postId);
}
