package com.oxn.aiPicturesStore.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.manager.FileManager;
import com.oxn.aiPicturesStore.mapper.PictureMapper;
import com.oxn.aiPicturesStore.model.dto.file.UploadPictureResult;
import com.oxn.aiPicturesStore.model.dto.picture.PictureQueryRequest;
import com.oxn.aiPicturesStore.model.dto.picture.PictureUploadRequest;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.PictureVO;
import com.oxn.aiPicturesStore.model.vo.UserVO;
import com.oxn.aiPicturesStore.service.PictureService;
import com.oxn.aiPicturesStore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private FileManager fileManager;

    @Autowired
    private UserService userService;

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, StatusCode.NO_AUTH_ERROR);
        // 用于判断是新增还是更新图片  
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 如果是更新图片，需要校验图片是否存在  
        if (pictureId != null) {
            boolean exists = this.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .exists();
            ThrowUtils.throwIf(!exists, StatusCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 上传图片，得到信息  
        // 按照用户 id 划分目录  
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 构造要入库的图片信息  
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        // 如果 pictureId 不为空，表示更新，否则是新增  
        if (pictureId != null) {
            // 如果是更新，需要补充 id 和编辑时间  
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, StatusCode.OPERATION_ERROR, "图片上传失败");
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
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id)&& !id.equals(0L), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
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
            User user=null;
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

}




