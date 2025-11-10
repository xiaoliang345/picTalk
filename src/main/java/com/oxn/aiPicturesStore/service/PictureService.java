package com.oxn.aiPicturesStore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxn.aiPicturesStore.enums.PictureReviewStatusEnum;
import com.oxn.aiPicturesStore.model.dto.picture.*;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.PictureVO;
import com.qcloud.cos.transfer.Upload;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.util.List;

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

    /**
     * 删除cos图片
     * @param picture
     */
    void deleteObject(Picture picture) throws MalformedURLException;

    /**
     * 按照颜色相似度查询图片
     *
     * @param spaceId spaceId
     * @param picColor 颜色
     * @param loginUser 登录的用户
     * @return 图片 vo 结合
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量编辑图片
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest,User loginUser);

    /**
     * 根据描述生成新图片
     * @param pictureUpdateByAIRequest
     * @param picture
     * @param loginUser
     */
    String pictureEditByAI(PictureUpdateByAIRequest pictureUpdateByAIRequest, Picture picture,User loginUser);

    /**
     * 删除图片
     * @param picture
     * @param loginUser
     * @return
     */
    Boolean deletePicture(Picture picture,User loginUser);

    /**
     * AI编辑图片
     * @param pictureUpdateByAIRequest
     * @return
     */
    void AiEditPicture(PictureUpdateByAIRequest pictureUpdateByAIRequest,Picture picture,User loginUser,String taskId);

    /**
     * 上传头像
     * @param multipartFile
     * @param loginUser
     * @return
     */
    String uploadAvatar(MultipartFile multipartFile, User loginUser);


}
