package com.oxn.aiPicturesStore.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.oxn.aiPicturesStore.config.CosClientConfig;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.manager.UploadProgressManage;
import com.oxn.aiPicturesStore.mapper.FileShareMapper;
import com.oxn.aiPicturesStore.model.entity.FileShare;
import com.oxn.aiPicturesStore.model.vo.UploadVO;
import com.oxn.aiPicturesStore.service.FileShareService;
import com.oxn.aiPicturesStore.utils.RandomCodeUtils;
import com.oxn.aiPicturesStore.common.BaseResponse;
import com.oxn.aiPicturesStore.common.ResultUtils;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.transfer.Transfer;
import com.qcloud.cos.transfer.TransferProgress;
import com.qcloud.cos.transfer.Upload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileShareController {

    @Resource
    private CosClientConfig cosClientConfig;

    @Autowired
    private COSClient cosClient;

    @Autowired
    private UploadProgressManage uploadProgressManage;

    @Autowired
    private FileShareService fileShareService;

    @Autowired
    private FileShareMapper fileShareMapper;

    @Autowired
    private RandomCodeUtils randomCodeUtils;

    private static final String FILE_PREFIX = "/fileShare/";

    /**
     * 图片访问前缀
     */
    @Value("${nginx.proxyUrl}")
    private String ImageAccessPrefix;


    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<UploadVO> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "上传文件不能为空");
        }

        try {
            // 保存临时文件
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);

            String key = file.getOriginalFilename();
            String authCode = randomCodeUtils.generateUniqueCode();

            // 使用 TransferManager 上传
            Upload upload = fileShareService.uploadFile(tempFile, FILE_PREFIX + authCode + "/" + key);

            // 启动进度监听（在后台线程）
            monitorUploadProgress(upload, authCode); // 你的监听函数

            // 将文件信息保存到数据库
            FileShare fileShare = new FileShare();
            fileShare.setShareCode(authCode);
            fileShare.setFileName(key);
            fileShare.setCreatedAt(LocalDateTime.now());
            // 设置过期时间为20分钟后
            fileShare.setExpiresAt(LocalDateTime.now().plusMinutes(20));
            fileShare.setIpAddress(null); // 可根据需要设置IP地址
            fileShareMapper.insert(fileShare);

            // 返回 authCode 给前端轮询
            UploadVO uploadVO = new UploadVO();
            uploadVO.setCode(authCode);
            uploadVO.setFileName(key);
            return ResultUtils.success(uploadVO);

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "上传文件失败:" + e.getMessage());
        }
    }


    /**
     * 获取文件的临时下载链接
     */
    @PostMapping("/download-url")
    public BaseResponse<String> getDownloadUrl(@RequestBody UploadVO uploadVO) {
        try {
            ThrowUtils.throwIf(uploadVO == null, StatusCode.PARAMS_ERROR, "参数错误");
            Long count = fileShareService.query().
                    eq("share_code", uploadVO.getCode()).
                    eq("file_name", uploadVO.getFileName())
                    .count();
            ThrowUtils.throwIf(count <= 0, StatusCode.PARAMS_ERROR, "文件不存在/已过期");
            String key = FILE_PREFIX + uploadVO.getCode() + '/' + uploadVO.getFileName();
            // 构造请求
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(cosClientConfig.getBucket(), key);
            // 设置链接 10 分钟后过期
            request.setExpiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000));

            // 生成签名 URL
            URL signedUrl = cosClient.generatePresignedUrl(request);
            String signedUrlString = signedUrl.toString();
            //替换为Nginx代理前缀
            signedUrlString = signedUrlString.replace(cosClientConfig.getHost(), ImageAccessPrefix);
            return ResultUtils.success(signedUrlString);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "生成下载链接失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/list/{code}")
    public BaseResponse<List<UploadVO>> listFiles(@PathVariable String code) {
        try {
            LambdaQueryWrapper<FileShare> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(FileShare::getShareCode, code)
                    .select(FileShare::getFileName, FileShare::getShareCode);
            List<FileShare> list = fileShareService.list(lambdaQueryWrapper);
            if (list.size() == 0) {
                throw new BusinessException(StatusCode.PARAMS_ERROR, "文件不存在/已过期");
            }
            List<UploadVO> uploadVOS = list.stream().map(fileShare -> {
                UploadVO uploadVO = new UploadVO();
                uploadVO.setFileName(fileShare.getFileName());
                uploadVO.setCode(fileShare.getShareCode());
                return uploadVO;
            }).collect(Collectors.toList());
            /*ListObjectsRequest request = new ListObjectsRequest();
            request.setBucketName(cosClientConfig.getBucket());
            request.setPrefix(FILE_PREFIX + code + "/"); // 👈 关键：指定前缀为 "test/"
            ObjectListing listing = cosClient.listObjects(request);
            List<COSObjectSummary> objectSummaries = listing.getObjectSummaries();
            if (ObjectUtils.isEmpty(objectSummaries)) {
                return ResultUtils.success(null);
            }
            List<String> collect = objectSummaries.stream()
                    .map(COSObjectSummary::getKey)
                    .collect(Collectors.toList());*/
            return ResultUtils.success(uploadVOS);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 进度监听函数
     *
     * @param transfer
     * @param authCode
     */
    private void monitorUploadProgress(Transfer transfer, String authCode) {
        new Thread(() -> {
            while (!transfer.isDone()) {
                try {
                    Thread.sleep(500);
                    TransferProgress progress = transfer.getProgress();

                    // ✅ 关键：只有 total > 0 才更新进度，避免 0/0 = NaN 或 100%
                    if (progress.getTotalBytesToTransfer() > 0) {
                        uploadProgressManage.updateProgress(authCode, progress);
                    }
                    // 否则不更新，前端保持“上传中”状态
                } catch (InterruptedException e) {
                    break;
                }
            }
            // 上传完成，确保最终状态是 100%
            uploadProgressManage.updateProgress(authCode, transfer.getProgress());
        }).start();
    }

    /**
     * 获取上传进度
     *
     * @param authCode
     * @return
     */
    @GetMapping("/upload/progress/{authCode}")
    public BaseResponse<Map<String, Object>> getUploadProgress(@PathVariable String authCode) {
        TransferProgress progress = uploadProgressManage.getProgress(authCode);
        if (progress == null) {
            throw new BusinessException(StatusCode.NOT_FOUND_ERROR, "未找到上传进度信息");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("uploaded", progress.getBytesTransferred());
        result.put("total", progress.getTotalBytesToTransfer());
        result.put("percent", progress.getPercentTransferred());
        return ResultUtils.success(result);
    }
}