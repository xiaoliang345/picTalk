package com.oxn.aiPicturesStore.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.oxn.aiPicturesStore.common.ResultUtils;
import com.oxn.aiPicturesStore.config.CosClientConfig;
import com.oxn.aiPicturesStore.constant.PictureConstant;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        //校验图片
        validPicture(multipartFile);
        //图片上传地址
        String originalFilename = multipartFile.getOriginalFilename();
        String uuid = RandomUtil.randomString(5);
        //拼接路径
        //TODO:测试能否使用getPrefix,getSuffix
        String fileName = String.format("%s.%s",uuid, FileUtil.getSuffix(originalFilename));
        String filePath = String.format("%s/%s", uploadPathPrefix, fileName);
        File file=null;
        try {
            // 上传文件
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file);
            //获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //获取宽高和宽高比
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round((double) width / height, 2).doubleValue();
            //封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost()+"/"+filePath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(width);
            uploadPictureResult.setPicHeight(height);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filePath, e);
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);
        }
    }

    /**
     * 校验图片
     *
     * @param multipartFile
     */
    public void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, StatusCode.SYSTEM_ERROR, "文件不能为空");
        // 校验文件大小
        long size = multipartFile.getSize();
        final int FILE_SIZE = 1024 * 1024 * 2;
        ThrowUtils.throwIf(size > FILE_SIZE, StatusCode.SYSTEM_ERROR, "文件大小不能超过2MB");
        //文件类型
        String contentType = multipartFile.getContentType();
        final List<String> FILE_TYPE = PictureConstant.FILE_TYPE;
        ThrowUtils.throwIf(!FILE_TYPE.contains(contentType), StatusCode.SYSTEM_ERROR, "文件类型错误");

    }

    /**
     * 删除临时文件
     *
     * @param file
     */
    public void deleteTempFile(File file) {
        if (file == null) return ;
        // 删除临时文件
        boolean delete = file.delete();
        if (!delete) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
}
