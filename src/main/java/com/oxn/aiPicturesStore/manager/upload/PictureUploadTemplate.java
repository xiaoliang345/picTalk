package com.oxn.aiPicturesStore.manager.upload;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.oxn.aiPicturesStore.config.CosClientConfig;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.manager.CosManager;
import com.oxn.aiPicturesStore.model.dto.file.UploadPictureResult;
import com.oxn.aiPicturesStore.model.dto.picture.PicttureBuildResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public abstract class PictureUploadTemplate {


    @Resource
    private CosManager cosManager;


    /**
     * 图片访问前缀
     */
    @Value("${nginx.proxyUrl}")
    private String ImageAccessPrefix;

    /**
     * 上传图片
     *
     * @param inputSource
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        //校验图片
        String fileType = validPicture(inputSource);
        //图片上传地址
        String originalFilename = getOriginalFilename(inputSource);
        String uuid = RandomUtil.randomString(5);
        String fileName = String.format("%s.%s", uuid, fileType);
        //拼接路径
        String filePath = String.format("%s/%s", uploadPathPrefix, fileName);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filePath, null);
            processFile(inputSource, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file, fileName);
            //获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            //获取压缩图、缩略图
            CIObject preObject = null;
            CIObject thumbnaiObject = null;
            if (!objectList.isEmpty()) {
                preObject = objectList.get(0);
                if (1 < objectList.size())
                    thumbnaiObject = objectList.get(1);
            }
            //原始图片key
            String originalFilekey = putObjectResult.getCiUploadResult().getOriginalInfo().getKey();

            PicttureBuildResult picttureBuildResult = new PicttureBuildResult();
            //整理返回参数
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round((double) width / height, 2).doubleValue();
            //没有缩略图则用预览图
            if (thumbnaiObject != null && thumbnaiObject.getKey() != null)
                picttureBuildResult.setThumbnailUrl(ImageAccessPrefix + "/" + thumbnaiObject.getKey());
            else {
                picttureBuildResult.setThumbnailUrl(ImageAccessPrefix + "/" + preObject.getKey());
            }
            picttureBuildResult.setUrl(ImageAccessPrefix + "/" + originalFilekey);
            picttureBuildResult.setPreviewUrl(ImageAccessPrefix + "/" + preObject.getKey());
            picttureBuildResult.setPicName(FileUtil.mainName(originalFilename));
            picttureBuildResult.setPicSize(FileUtil.size(file));
            picttureBuildResult.setPicWidth(width);
            picttureBuildResult.setPicHeight(height);
            picttureBuildResult.setPicScale(picScale);
            picttureBuildResult.setPicFormat(fileType);
            picttureBuildResult.setPicColor(imageInfo.getAve());
            return buildResult(picttureBuildResult);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filePath, e);
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);
        }
    }

    /**
     * 封装返回对象
     *
     * @param picttureBuildResult
     * @return
     */
    private UploadPictureResult buildResult( PicttureBuildResult picttureBuildResult) {


        //封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        BeanUtils.copyProperties(picttureBuildResult, uploadPictureResult);
        return uploadPictureResult;
    }

    protected abstract void processFile(Object inputSource, File file) throws IOException;

    protected abstract String getOriginalFilename(Object inputSource);

    protected abstract String validPicture(Object inputSource);


    /**
     * 删除临时文件
     *
     * @param file
     */
    public void deleteTempFile(File file) {
        if (file == null) return;
        // 删除临时文件
        boolean delete = file.delete();
        if (!delete) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

}