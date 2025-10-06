package com.oxn.aiPicturesStore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxn.aiPicturesStore.enums.PictureReviewStatusEnum;
import com.oxn.aiPicturesStore.model.dto.picture.PictureQueryRequest;
import com.oxn.aiPicturesStore.model.dto.picture.PictureReviewRequest;
import com.oxn.aiPicturesStore.model.dto.picture.PictureUploadByBatchRequest;
import com.oxn.aiPicturesStore.model.dto.picture.PictureUploadRequest;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.PictureVO;
import com.qcloud.cos.transfer.Upload;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 34576
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-09-24 20:09:58
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 获取查询条件
     *
     *
     * */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片(单条脱敏）
     * @param picture
     * @param httpServletRequest
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest httpServletRequest);

    /**
     * 获取图片(多条脱敏）
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVO(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 审核图片
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 自动填充参数
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture,User loginUser);

    /**
     * 图片批量抓取上传
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                 User loginUser);
}
