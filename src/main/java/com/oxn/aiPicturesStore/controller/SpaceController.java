package com.oxn.aiPicturesStore.controller;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.oxn.aiPicturesStore.annotation.AuthCheck;
import com.oxn.aiPicturesStore.common.BaseResponse;
import com.oxn.aiPicturesStore.common.DeleteRequest;
import com.oxn.aiPicturesStore.common.ResultUtils;
import com.oxn.aiPicturesStore.constant.UserConstant;
import com.oxn.aiPicturesStore.enums.SpaceLevelEnum;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.manager.auth.SpaceUserAuthManager;
import com.oxn.aiPicturesStore.manager.auth.annotation.SaSpaceCheckPermission;
import com.oxn.aiPicturesStore.manager.auth.model.SpaceUserPermissionConstant;
import com.oxn.aiPicturesStore.model.dto.space.*;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.SpaceVO;
import com.oxn.aiPicturesStore.service.SpaceService;
import com.oxn.aiPicturesStore.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private UserService userService;

    @Autowired
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 创建空间
     *
     * @param spaceAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, StatusCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long spaceId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(spaceId);
    }


    /**
     * 删除空间
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) throws MalformedURLException {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在  
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, StatusCode.NOT_FOUND_ERROR);
        // 操作数据库  
        boolean result = spaceService.removeById(id);
        ThrowUtils.throwIf(!result, StatusCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新空间（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest,
                                             HttpServletRequest request) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换  
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        spaceService.fillSpaceBySpaceLevel(space);
        // 数据校验  
        spaceService.validSpace(space, false);
        // 判断是否存在  
        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, StatusCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 操作数据库  
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, StatusCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取空间（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, StatusCode.PARAMS_ERROR);
        // 查询数据库  
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, StatusCode.NOT_FOUND_ERROR);
        // 获取封装类  
        return ResultUtils.success(space);
    }

    /**
     * 根据 id 获取空间（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, StatusCode.PARAMS_ERROR);
        // 查询数据库  
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, StatusCode.NOT_FOUND_ERROR);
        SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        spaceVO.setPermissionList(permissionList);
        return ResultUtils.success(spaceVO);
    }

    /**
     * 分页获取空间列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 查询数据库  
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    /**
     * 分页获取空间列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                         HttpServletRequest request) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        // 限制爬虫  
        ThrowUtils.throwIf(size > 20, StatusCode.PARAMS_ERROR);
        // 查询数据库
        User loginUser = userService.getLoginUser(request);
        spaceQueryRequest.setUserId(loginUser.getId());
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        // 获取封装类  
        return ResultUtils.success(spaceService.getSpaceVO(spacePage, request));
    }


    /**
     * 编辑空间（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换  
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        // 设置编辑时间  
        space.setEditTime(new Date());
        // 判断是否存在  
        long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, StatusCode.NOT_FOUND_ERROR);
        // 操作数据库  
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, StatusCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取空间类型信息
     *
     * @return
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        SpaceLevelEnum[] values = SpaceLevelEnum.values();
        List<SpaceLevel> collect = Arrays.stream(values).map(value ->
                new SpaceLevel(value.getText(), value.getValue(), value.getMaxCount(), value.getMaxSize())).collect(Collectors.toList());
        return ResultUtils.success(collect);
    }
}
