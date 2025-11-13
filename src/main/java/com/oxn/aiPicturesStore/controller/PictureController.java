package com.oxn.aiPicturesStore.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.oxn.aiPicturesStore.annotation.AuthCheck;
import com.oxn.aiPicturesStore.common.BaseResponse;
import com.oxn.aiPicturesStore.common.DeleteRequest;
import com.oxn.aiPicturesStore.common.ResultUtils;
import com.oxn.aiPicturesStore.constant.PictureConstant;
import com.oxn.aiPicturesStore.constant.UserConstant;
import com.oxn.aiPicturesStore.enums.PictureReviewStatusEnum;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.enums.TaskStatus;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.manager.AiTaskManager;
import com.oxn.aiPicturesStore.manager.CosManager;
import com.oxn.aiPicturesStore.manager.auth.SpaceUserAuthManager;
import com.oxn.aiPicturesStore.manager.auth.StpKit;
import com.oxn.aiPicturesStore.manager.auth.annotation.SaSpaceCheckPermission;
import com.oxn.aiPicturesStore.manager.auth.model.SpaceUserPermissionConstant;
import com.oxn.aiPicturesStore.model.dto.picture.*;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.PictureTagCategory;
import com.oxn.aiPicturesStore.model.vo.PictureVO;
import com.oxn.aiPicturesStore.service.PictureService;
import com.oxn.aiPicturesStore.service.SpaceService;
import com.oxn.aiPicturesStore.service.UserService;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Autowired
    private PictureService pictureService;

    @Autowired
    private UserService userService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SpaceUserAuthManager spaceUserAuthManager;

    //LOCAL_CACHE配置
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(10L, TimeUnit.SECONDS)
                    .build();

    @Autowired
    private AiTaskManager aiTaskManager;


    /**
     * 图片上传（文件）
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile,
                                                 PictureUploadRequest pictureUploadRequest,
                                                 HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 图片上传（URL）
     *
     * @param pictureUploadRequest
     * @return
     */
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(@RequestBody PictureUploadRequest pictureUploadRequest,
                                                      HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过URL批量抓取
     *
     * @param pictureUploadByBatchRequest
     * @return
     */
    @PostMapping("/upload/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                      HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, StatusCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpServletRequest);
        Integer count = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(count);
    }

    /**
     * 删除图片
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) throws MalformedURLException {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Picture oldPicture = pictureService.getById(id);
        Boolean b = pictureService.deletePicture(oldPicture, loginUser);
        return ResultUtils.success(b);
    }

    /**
     * 更新图片（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
                                               HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换  
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 注意将 list 转为 string  
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验  
        pictureService.validPicture(picture);
        // 判断是否存在  
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, StatusCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        //补充审核参数
        pictureService.fillReviewParams(picture, loginUser);
        // 操作数据库  
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, StatusCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, StatusCode.PARAMS_ERROR);
        // 查询数据库  
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, StatusCode.NOT_FOUND_ERROR);
        // 获取封装类  
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        // 1. 参数校验
        ThrowUtils.throwIf(id <= 0, StatusCode.PARAMS_ERROR);

        // 2. 查询图片
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, StatusCode.NOT_FOUND_ERROR);

        // 3. 获取空间信息（仅当需要时）
        Space space = null;
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, StatusCode.NOT_FOUND_ERROR);
        }

        // 4. 获取当前用户权限列表（用于前端展示）
        //User loginUser = userService.getLoginUser(request);
        //List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);

        // 5. 构建返回 VO
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        //pictureVO.setPermissionList(permissionList);

        return ResultUtils.success(pictureVO);
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库  
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫  
        ThrowUtils.throwIf(size > 20, StatusCode.PARAMS_ERROR);
        //普通用户只能看到审核通过的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        //如果spaceId为null，则查询所有图片，否则查询指定空间下的图片， 需要当前用户是空间管理员或者空间成员
        Long spaceId = pictureQueryRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, StatusCode.NOT_FOUND_ERROR);
            boolean b = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!b, StatusCode.NO_AUTH_ERROR);
        }

        // 查询数据库  
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类  
        return ResultUtils.success(pictureService.getPictureVO(picturePage, request));
    }

    /**
     * 分页获取图片列表（封装类，有缓存）
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWitchCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                                       HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, StatusCode.PARAMS_ERROR);
        //普通用户只能看到审核通过的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        //根据是否有缓存来决定是否查询数据库
        String jsonStr = JSONUtil.toJsonStr(pictureQueryRequest);
        String md5DigestAsHex = DigestUtils.md5DigestAsHex(jsonStr.getBytes());
        String cacheKey = String.format("aiPicturesStore:listPictureVOByPage:%s", md5DigestAsHex);
        String CaffaineCacheValue = LOCAL_CACHE.getIfPresent(cacheKey);
        //有Caffaine缓存
        if (CaffaineCacheValue != null) {
            Page<PictureVO> page = JSONUtil.toBean(CaffaineCacheValue, Page.class);
            return ResultUtils.success(page);
        }
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String RedisCacheValue = valueOperations.get(cacheKey);
        //有Redis缓存
        if (RedisCacheValue != null) {
            LOCAL_CACHE.put(cacheKey, RedisCacheValue);
            Page<PictureVO> page = JSONUtil.toBean(RedisCacheValue, Page.class);
            return ResultUtils.success(page);
        }
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        Page<PictureVO> pictureVO = pictureService.getPictureVO(picturePage, request);
        String resCache = JSONUtil.toJsonStr(pictureVO);
        //将数据保存到caffaine和redis
        LOCAL_CACHE.put(cacheKey, resCache);
        int randomInt = 10 + RandomUtil.randomInt(0, 10);
        valueOperations.set(cacheKey, resCache, randomInt, TimeUnit.SECONDS);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        pictureService.validPicture(picture);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, StatusCode.NOT_FOUND_ERROR);
        if(!ObjectUtils.isEmpty(oldPicture.getSpaceId())){
            picture.setSpaceId(oldPicture.getSpaceId());
        }
        //补充审核参数
        pictureService.fillReviewParams(picture, loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, StatusCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取标签分类列表
     */
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        pictureTagCategory.setCategoryMap(PictureConstant.categoryMap);
        pictureTagCategory.setTagMap(PictureConstant.tagMap);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 图片审核
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, StatusCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 根据图片颜色搜索
     */
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest,
                                                              HttpServletRequest request) {
        ThrowUtils.throwIf(searchPictureByColorRequest == null, StatusCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        String picColor = searchPictureByColorRequest.getPicColor();
        List<PictureVO> pictureVOList = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(pictureVOList);
    }


    /**
     * 批量编辑图片
     */
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest,
                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, StatusCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 使用 AI 编辑图片（异步）
     */
    @PostMapping("/ai/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<String> AiEditPicture(@RequestBody PictureUpdateByAIRequest pictureUpdateByAIRequest,
                                              HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUpdateByAIRequest == null, StatusCode.PARAMS_ERROR);
        if (pictureUpdateByAIRequest == null || pictureUpdateByAIRequest.getId() <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        String description = pictureUpdateByAIRequest.getDescription();
        if (StrUtil.isBlank(description)) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "描述不能为空");
        }

        Picture picture = pictureService.getById(pictureUpdateByAIRequest.getId());
        if (picture == null) {
            throw new BusinessException(StatusCode.OPERATION_ERROR, "图片不存在");
        }
        User loginUser = userService.getLoginUser(request);

        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String taskCountStr = valueOperations.get("task_count");
        long taskCount = 0;
        Long ttl = stringRedisTemplate.getExpire("task_count", TimeUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        long between = ChronoUnit.SECONDS.between(now, endOfDay);
        // 如果时间过期或者没有值，则设置时间并初始化任务数量为1
        if (ttl.equals(-1L) || StringUtils.isEmpty(taskCountStr)) {
            valueOperations.set("task_count", "1", between, TimeUnit.SECONDS);
        } else {
            taskCount = Long.parseLong(taskCountStr);
            if (taskCount >= 7) {
                return new BaseResponse<>(StatusCode.OPERATION_ERROR.getCode(), null, "任务数量已达上限");
            }
            taskCount++;
            valueOperations.set("task_count", String.valueOf(taskCount), between, TimeUnit.SECONDS);
        }
        String taskId = aiTaskManager.createTask(description);
        pictureService.AiEditPicture(pictureUpdateByAIRequest, picture, loginUser, taskId);
        return ResultUtils.success(taskId);
    }

    /**
     * 查询 AI 图片生成状态
     */
    @GetMapping("/ai/status")
    public BaseResponse<AiImageTaskResult> getAiPictureStatus(@RequestParam String taskId) {
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "任务ID不能为空");
        }

        AiImageTaskResult taskResult = aiTaskManager.getTask(taskId);
        if (taskResult == null) {
            throw new BusinessException(StatusCode.OPERATION_ERROR, "任务不存在或已过期");
        }
        return ResultUtils.success(taskResult);
    }

    /**
     * 头像上传
     */
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile multipartFile,
                                              HttpServletRequest request) {
        ThrowUtils.throwIf(multipartFile == null, StatusCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        String avatar = pictureService.uploadAvatar(multipartFile, loginUser);
        return ResultUtils.success(avatar);
    }
}
