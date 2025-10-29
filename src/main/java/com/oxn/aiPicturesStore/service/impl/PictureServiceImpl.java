package com.oxn.aiPicturesStore.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxn.aiPicturesStore.constant.PictureConstant;
import com.oxn.aiPicturesStore.enums.PictureReviewStatusEnum;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.manager.CosManager;
import com.oxn.aiPicturesStore.manager.FileManager;
import com.oxn.aiPicturesStore.manager.ImageEditService;
import com.oxn.aiPicturesStore.manager.upload.FilePictureUpload;
import com.oxn.aiPicturesStore.manager.upload.PictureUploadTemplate;
import com.oxn.aiPicturesStore.manager.upload.UrlPictureUpload;
import com.oxn.aiPicturesStore.mapper.PictureMapper;
import com.oxn.aiPicturesStore.model.dto.file.UploadPictureResult;
import com.oxn.aiPicturesStore.model.dto.picture.*;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.PictureVO;
import com.oxn.aiPicturesStore.model.vo.UserVO;
import com.oxn.aiPicturesStore.service.PictureService;
import com.oxn.aiPicturesStore.service.SpaceService;
import com.oxn.aiPicturesStore.service.UserService;
import com.oxn.aiPicturesStore.utils.HexColorSimilarityUtil;
import org.bouncycastle.asn1.cms.PasswordRecipientInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 34576
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-09-24 20:09:58
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    @Autowired
    private UserService userService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private FilePictureUpload filePictureUpload;

    @Autowired
    private UrlPictureUpload urlPictureUpload;

    @Autowired
    private CosManager cosManager;

    @Autowired
    private ImageEditService imageEditService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, StatusCode.NO_AUTH_ERROR);
        // 用于判断是新增还是更新图片
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 如果是更新图片，需要校验图片是否存在
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            if (oldPicture == null)
                throw new BusinessException(StatusCode.NOT_FOUND_ERROR, "图片不存在");
            //仅本人或管理员可以修改图片
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(StatusCode.NO_AUTH_ERROR);
            }
        }
        // 按照用户 id 划分目录
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        // 上传图片，得到信息
        PictureUploadTemplate uploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            uploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = uploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 构造要入库的图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        //如果指定了文件名前缀则使用，没有的话就使用从URL中解析的名称
        if (StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picture.setName(pictureUploadRequest.getPicName());
        } else {
            picture.setName(uploadPictureResult.getPicName());
        }
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setPicColor(uploadPictureResult.getPicColor());
        picture.setUserId(loginUser.getId());
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            picture.setSpaceId(spaceId);
        }
        //补充审核参数
        this.fillReviewParams(picture, loginUser);
        // 如果 pictureId 不为空，表示更新，否则是新增
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, StatusCode.OPERATION_ERROR, "图片上传失败");
            if (spaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, spaceId)
                        .setSql("totalCount=totalCount+" +1)
                        .setSql("totalSize=totalSize+" + picture.getPicSize())
                        .update();
                ThrowUtils.throwIf(!update, StatusCode.OPERATION_ERROR, "空间信息更新失败");
            }
            return status;
        });
        return PictureVO.objToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Long spaceId = pictureQueryRequest.getSpaceId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id) && !id.equals(0L), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        if (ObjUtil.isNotEmpty(spaceId)) {
            // spaceId 有值：只查等于该值的
            queryWrapper.eq("spaceId", spaceId);
        } else {
            // spaceId 为空（null 或 empty）：只查 spaceId 为 NULL 的记录
            queryWrapper.isNull("spaceId");
        }
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId) && !reviewerId.equals(0L), "reviewerId", reviewerId);
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.le(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);

        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest httpServletRequest) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVo = userService.getUserVo(user);
            pictureVO.setUser(userVo);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVO(Page<Picture> picturePage, HttpServletRequest request) {
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(),
                picturePage.getSize(), picturePage.getTotal());
        List<Picture> records = picturePage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return pictureVOPage;
        }
        //封装对象列表
        List<PictureVO> pictureVOList = records.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        Set<Long> userIdSet = records.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userMap.containsKey(userId)) {
                user = userMap.get(userId).get(0);
                pictureVO.setUser(userService.getUserVo(user));
            }
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, StatusCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), StatusCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, StatusCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, StatusCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(pictureReviewRequest == null, StatusCode.PARAMS_ERROR);
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        if (id == null || reviewMessage == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatus)) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        //判断图片是否存在
        Picture oldPicture = this.getById(pictureReviewRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, StatusCode.PARAMS_ERROR);
        //判断是否重复校验
        if (oldPicture.getReviewStatus().equals(pictureReviewRequest.getReviewStatus())) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "请勿重复校验");
        }
        //更新数据
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, picture);
        boolean b = this.updateById(picture);
        ThrowUtils.throwIf(!b, StatusCode.OPERATION_ERROR);
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
        } else {
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        //参数校验
        int count = pictureUploadByBatchRequest.getCount();
        String searchText = pictureUploadByBatchRequest.getSearchText();
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = pictureUploadByBatchRequest.getSearchText();
        }
        ThrowUtils.throwIf(count > 10, StatusCode.PARAMS_ERROR, "抓取数量不能超过十条");
        //抓取图片
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText + "壁纸");
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败" + e);
            throw new BusinessException(StatusCode.OPERATION_ERROR, "页面抓取失败");
        }
        // 选择所有img标签
        Elements imgElements = document.select("img");

        // 遍历img标签，提取src属性，直到达到count数量或遍历完成
        for (Element img : imgElements) {
            String imgUrl = img.attr("src");
            // 处理相对路径，转换为绝对路径
            if (!imgUrl.startsWith("http://") && !imgUrl.startsWith("https://")) {
                // 利用Jsoup的absUrl方法自动补全绝对路径
                imgUrl = img.absUrl("src");
            }
            // 过滤空URL
            if (!imgUrl.isEmpty()) {
                try {
                    imgUrl = imgUrl.split("\\?")[0];
                    // 达到指定数量则停止
                    PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                    pictureUploadRequest.setPicName(namePrefix + count);

                    PictureVO pictureVO = this.uploadPicture(imgUrl, pictureUploadRequest, loginUser);
                    if (ObjUtil.isNotEmpty(pictureVO)) {
                        count--;
                        if (count == 0) break;
                    }
                } catch (Exception e) {
                    log.error("图片抓取失败" + imgUrl);
                    throw new BusinessException(StatusCode.OPERATION_ERROR, "图片抓取失败");
                }
            }
        }
        return count;
    }

    @Override
    public void deleteObject(Picture picture) {
        String path = null;
        try {
            path = new URL(picture.getUrl()).getPath();
            cosManager.deleteObject(path);
            //删除缩略图
            if (!picture.getThumbnailUrl().isEmpty())
                path = new URL(picture.getThumbnailUrl()).getPath();
            cosManager.deleteObject(path);
        } catch (MalformedURLException e) {
            throw new BusinessException(StatusCode.OPERATION_ERROR, "服务器图片删除失败");
        }
    }

    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        //参数校验
        ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), StatusCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, StatusCode.NOT_LOGIN_ERROR);
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, StatusCode.PARAMS_ERROR, "空间不存在");
        //权限校验
        if (!Objects.equals(space.getUserId(), loginUser.getId())) {
            throw new BusinessException(StatusCode.NO_AUTH_ERROR);
        }
        //查询空间下有色调的图片
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .isNotNull(Picture::getPicColor)
                .list();
        // 处理空列表：直接返回空列表
        if (pictureList == null || pictureList.isEmpty()) {
            return new ArrayList<>();
        }

        // 遍历列表，计算每个图片与目标颜色的相似度，按相似度降序排序
        return pictureList.stream()
                .map(PictureVO::objToVo)
                .sorted((pic1, pic2) -> {
                    // 计算两个图片与目标颜色的相似度（使用HSV算法，更符合人眼感知）
                    int similarity1 = HexColorSimilarityUtil.calculateSimilarityByHSV(picColor, pic1.getPicColor());
                    int similarity2 = HexColorSimilarityUtil.calculateSimilarityByHSV(picColor, pic2.getPicColor());
                    // 按相似度从大到小排序（降序）
                    return Integer.compare(similarity2, similarity1);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        //参数校验
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();
        ThrowUtils.throwIf(spaceId == null || pictureIdList.isEmpty(), StatusCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, StatusCode.NO_AUTH_ERROR);
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "空间不存在");
        }
        //校验空间权限
        if (!space.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(StatusCode.NO_AUTH_ERROR, "当前空间无权限");
        }
        //构建查询条件
        List<Picture> list = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId)
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
                .list();
        if (list.isEmpty()) return;
        list.forEach(item -> {
            if (StrUtil.isNotBlank(category)) item.setCategory(category);
            if (!tags.isEmpty()) item.setTags(tags.toString());
        });
        String nameRule = pictureEditByBatchRequest.getNameRule();
        fillPictureWithNameRule(list, nameRule);
        boolean b = this.updateBatchById(list);
        ThrowUtils.throwIf(!b, StatusCode.OPERATION_ERROR);
    }

    @Override
    public String pictureUpdateByAI(String description, Picture picture) {
        String newPictureUrl = imageEditService.editImage(picture.getUrl(), description);
        return newPictureUrl;
    }

    @Override
    public Boolean deletePicture(Picture picture, User loginUser) {
        ThrowUtils.throwIf(picture == null, StatusCode.NOT_FOUND_ERROR);
        // 仅管理员可删除
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(StatusCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = this.removeById(picture.getId());
        ThrowUtils.throwIf(!result, StatusCode.OPERATION_ERROR);
        //如果删除的为空间图片，则修改空间容量
        if (picture.getSpaceId() != null) {
            boolean update = spaceService.lambdaUpdate()
                    .eq(Space::getId, picture.getSpaceId())
                    .setSql("totalCount=totalCount-" + 1)
                    .setSql("totalSize=totalSize-" + picture.getPicSize())
                    .update();
            ThrowUtils.throwIf(!update, StatusCode.OPERATION_ERROR,"空间容量更新失败");
        }
        this.deleteObject(picture);
        return true;
    }

    /**
     * 根据nameRule命名：图片{序号}
     *
     * @param list
     * @param nameRule
     */
    public void fillPictureWithNameRule(List<Picture> list, String nameRule) {
        if (StrUtil.isBlank(nameRule)) {
            return;
        }
        int count = 1;
        try {
            for (Picture item : list) {
                item.setName(nameRule.replace("{序号}", String.valueOf(count++)));
            }
        } catch (Exception e) {
            throw new BusinessException(StatusCode.OPERATION_ERROR, "名称解析错误");
        }
    }

}




