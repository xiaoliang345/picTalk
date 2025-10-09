package com.oxn.aiPicturesStore.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.oxn.aiPicturesStore.constant.PictureConstant;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * url上传
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {

    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, file);
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        return FileUtil.getName(fileUrl);
    }

    @Override
    protected String validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), StatusCode.PARAMS_ERROR);
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "文件url格式有误");
        }
        ThrowUtils.throwIf(!fileUrl.startsWith("http") && !fileUrl.startsWith("https"),
                StatusCode.PARAMS_ERROR, "请求协议出错");

        HttpResponse response = null;
        try {
            //发送HEAD请求验证文件
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return "";
            }
            //判断文件类型
            String type = response.header("Content-Type");
            if (StrUtil.isNotBlank(type)) {
                ThrowUtils.throwIf(!PictureConstant.FILE_TYPE.contains(type), StatusCode.PARAMS_ERROR, "文件类型有误");
            }
            //TODO:判断文件大小
            String length = response.header("Content-Length");
            if (StrUtil.isNotBlank(length)) {
                long parseLong = Long.parseLong(length);
                long maxSize = 2 * 1024 * 1024;
                ThrowUtils.throwIf(parseLong > maxSize, StatusCode.PARAMS_ERROR, "文件大小不能超过2M");
            }
            //只返回文件类型
            return type.split("/")[1];
        } catch (Exception e) {
            throw e;
        } finally {
            if (response != null) response.close();
        }
    }
}
