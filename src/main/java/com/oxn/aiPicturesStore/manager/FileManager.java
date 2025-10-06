package com.oxn.aiPicturesStore.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Service
@Slf4j
@Deprecated
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
        String fileName = String.format("%s.%s", uuid, FileUtil.getSuffix(originalFilename));
        String filePath = String.format("%s/%s", uploadPathPrefix, fileName);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filePath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file,fileName);
            //获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //获取宽高和宽高比
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round((double) width / height, 2).doubleValue();
            //封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + filePath);
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
        if (file == null) return;
        // 删除临时文件
        boolean delete = file.delete();
        if (!delete) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

    /**
     *
     *
     * @param fileUrl
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPictureByUrl(String fileUrl, String uploadPathPrefix) {
        validPicture(fileUrl);
        //图片上传地址
        String originalFilename = FileUtil.mainName(fileUrl);
        String uuid = RandomUtil.randomString(5);
        //拼接路径
        String fileName = String.format("%s.%s", uuid, FileUtil.getSuffix(originalFilename));
        String filePath = String.format("%s/%s", uploadPathPrefix, fileName);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filePath, null);
            HttpUtil.downloadFile(fileUrl, file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, file, fileName);
            //获取图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //获取宽高和宽高比
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round((double) width / height, 2).doubleValue();
            //封装返回结果
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + filePath);
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
     * 校验URL图片
     *
     * @param fileUrl
     */
    private void validPicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), StatusCode.PARAMS_ERROR);
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "文件url格式有误");
        }
        ThrowUtils.throwIf(!fileUrl.startsWith("http") && !fileUrl.startsWith("https"),
                StatusCode.PARAMS_ERROR, "请求协议出错");

        HttpResponse response=null;
        try{
            //发送HEAD请求验证文件
            response= HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if(response.getStatus()!= HttpStatus.HTTP_OK){
                return;
            }
            //判断文件类型
            String type = response.header("Content-Type");
            if(StrUtil.isNotBlank(type)){
                ThrowUtils.throwIf(!PictureConstant.FILE_TYPE.contains(type),StatusCode.PARAMS_ERROR,"文件类型有误");
            }
            //判断文件大小
            String length = response.header("Content-Length");
            if(StrUtil.isNotBlank(length)){
                long parseLong = Long.parseLong(length);
                long maxSize=2*1024*1024;
                ThrowUtils.throwIf(parseLong>maxSize,StatusCode.PARAMS_ERROR,"文件大小不能超过2M");
            }
        }
        catch (Exception e){
            throw e;
        }
        finally {
            if(response!=null)response.close();
        }

    }
}
