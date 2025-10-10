package com.oxn.aiPicturesStore.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.oxn.aiPicturesStore.config.CosClientConfig;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.manager.CosManager;
import com.oxn.aiPicturesStore.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

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
            CIObject ciObject = null;
            CIObject thumbnaiObject = null;
            if (!objectList.isEmpty()) {
                ciObject = objectList.get(0);
                if (1 < objectList.size())
                    thumbnaiObject = objectList.get(1);
            }
            return buildResult(imageInfo, originalFilename, file, ciObject, thumbnaiObject);
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
     * @param imageInfo
     * @param originalFilename
     * @param file
     * @return
     */
    private UploadPictureResult buildResult(ImageInfo imageInfo, String originalFilename, File file, CIObject ciObject, CIObject thumbnaiObject) {
        //获取宽高和宽高比
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();
        double picScale = NumberUtil.round((double) width / height, 2).doubleValue();
        //封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        //没有缩略图则用压缩图
        if (thumbnaiObject != null && thumbnaiObject.getKey() != null)
            uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnaiObject.getKey());
        else {
            uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + ciObject.getKey());
        }
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + ciObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(width);
        uploadPictureResult.setPicHeight(height);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
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
