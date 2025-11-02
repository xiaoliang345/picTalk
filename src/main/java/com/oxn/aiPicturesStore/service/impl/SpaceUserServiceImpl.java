package com.oxn.aiPicturesStore.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxn.aiPicturesStore.enums.SpaceRoleEnum;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.model.dto.spaceuser.SpaceUserAddRequest;
import com.oxn.aiPicturesStore.model.dto.spaceuser.SpaceUserQueryRequest;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.SpaceUser;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.SpaceUserVO;
import com.oxn.aiPicturesStore.model.vo.SpaceVO;
import com.oxn.aiPicturesStore.model.vo.UserVO;
import com.oxn.aiPicturesStore.service.SpaceService;
import com.oxn.aiPicturesStore.service.SpaceUserService;
import com.oxn.aiPicturesStore.mapper.SpaceUserMapper;
import com.oxn.aiPicturesStore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 34576
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
 * @createDate 2025-10-24 13:36:56
 */
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
        implements SpaceUserService {

    @Autowired
    @Lazy
    private SpaceService spaceService;

    @Autowired
    private UserService userService;

    @Autowired
    private SpaceUserMapper spaceUserMapper;

    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest, User loginUser) {
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserAddRequest, spaceUser);
        //参数校验
        validSpaceUser(spaceUser, true, loginUser);
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, StatusCode.PARAMS_ERROR, "用户不存在");
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, StatusCode.PARAMS_ERROR, "空间不存在");
        boolean save = this.save(spaceUser);
        ThrowUtils.throwIf(!save, StatusCode.SYSTEM_ERROR, "保存失败");
        return spaceUser.getId();
    }

    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StrUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        return queryWrapper;
    }

    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser) {
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        Long userId = spaceUserVO.getUserId();
        Long spaceId = spaceUserVO.getSpaceId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVo(user);
            spaceUserVO.setUser(userVO);
        }
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            SpaceVO spaceVO = new SpaceVO();
            BeanUtil.copyProperties(space, spaceVO);
            spaceUserVO.setSpace(spaceVO);
        }
        return spaceUserVO;
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVO(List<SpaceUser> spacePage) {
        if (CollUtil.isEmpty(spacePage)) {
            return Collections.emptyList();
        }
        List<SpaceUserVO> spaceUserVOList = spacePage.stream()
                .map(SpaceUserVO::objToVo)
                .collect(Collectors.toList());
        //封装对象列表
        Set<Long> userIdSet = spaceUserVOList.stream().map(SpaceUserVO::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        spaceUserVOList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            User user = null;
            if (userMap.containsKey(userId)) {
                user = userMap.get(userId).get(0);
                Space space = spaceService.getById(spaceUserVO.getSpaceId());
                spaceUserVO.setSpace(SpaceVO.objToVo(space));
                spaceUserVO.setUser(userService.getUserVo(user));
            }
        });
        return spaceUserVOList;
    }

    @Override
    public void validSpaceUser(SpaceUser spaceUser, Boolean add,User loginUser) {
        Long id = spaceUser.getId();
        String spaceRole = spaceUser.getSpaceRole();
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        if (!add) {
            if (id == null || id <= 0) {
                throw new BusinessException(StatusCode.PARAMS_ERROR, "id不能为空");
            }
        }
        SpaceRoleEnum enumByValue = SpaceRoleEnum.getEnumByValue(spaceRole);
        if (spaceRole == null || enumByValue == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "空间类型错误");
        }

        if (spaceId == null || spaceId <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "空间id不能为空");
        }
        if (userId == null || userId <= 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "用户id不能为空");
        }

    }
}




