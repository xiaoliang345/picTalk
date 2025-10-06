package com.oxn.aiPicturesStore.manager.upload;

import com.oxn.aiPicturesStore.constant.PictureConstant;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 文件上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate{


    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected String validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, StatusCode.SYSTEM_ERROR, "文件不能为空");
        // 校验文件大小
        long size = multipartFile.getSize();
        final int FILE_SIZE = 1024 * 1024 * 2;
        ThrowUtils.throwIf(size > FILE_SIZE, StatusCode.SYSTEM_ERROR, "文件大小不能超过2MB");
        //文件类型
        String contentType = multipartFile.getContentType();
        final List<String> FILE_TYPE = PictureConstant.FILE_TYPE;
        ThrowUtils.throwIf(!FILE_TYPE.contains(contentType), StatusCode.SYSTEM_ERROR, "文件类型错误");
        return contentType.split("/")[1];
    }
}
