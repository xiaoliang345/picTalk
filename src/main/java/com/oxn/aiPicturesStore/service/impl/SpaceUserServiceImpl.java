package com.oxn.aiPicturesStore.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxn.aiPicturesStore.common.BaseResponse;
import com.oxn.aiPicturesStore.enums.SpaceRoleEnum;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.model.dto.spaceuser.InviteUserRequest;
import com.oxn.aiPicturesStore.model.dto.spaceuser.SpaceUserAddRequest;
import com.oxn.aiPicturesStore.model.dto.spaceuser.SpaceUserQueryRequest;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.SpaceUser;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.IniteInfoVO;
import com.oxn.aiPicturesStore.model.vo.SpaceUserVO;
import com.oxn.aiPicturesStore.model.vo.SpaceVO;
import com.oxn.aiPicturesStore.model.vo.UserVO;
import com.oxn.aiPicturesStore.service.SpaceService;
import com.oxn.aiPicturesStore.service.SpaceUserService;
import com.oxn.aiPicturesStore.mapper.SpaceUserMapper;
import com.oxn.aiPicturesStore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    private StringRedisTemplate stringRedisTemplate;

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
    public void validSpaceUser(SpaceUser spaceUser, Boolean add, User loginUser) {
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

    @Override
    public Long inviteUser(Long spaceId, User loginUser) {
        LambdaQueryWrapper<SpaceUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpaceUser::getSpaceId, spaceId);
        queryWrapper.eq(SpaceUser::getUserId, loginUser.getId());
        SpaceUser spaceUser = this.getOne(queryWrapper);
        ThrowUtils.throwIf(spaceUser != null, StatusCode.PARAMS_ERROR, "该用户已加入空间，请勿重复添加");
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, StatusCode.PARAMS_ERROR, "空间不存在");
        spaceUser = new SpaceUser();
        spaceUser.setSpaceRole(SpaceRoleEnum.VIEWER.getValue());
        spaceUser.setSpaceId(spaceId);
        spaceUser.setUserId(loginUser.getId());
        boolean save = this.save(spaceUser);
        return spaceId;
    }

    @Override
    public String createIniteLink(Long spaceId, User loginUser) {
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, StatusCode.PARAMS_ERROR, "空间不存在");
        //将链接信息存入redis20分钟内有效
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String string = UUID.randomUUID().toString();
        IniteInfoVO initeInfoVO = new IniteInfoVO();
        initeInfoVO.setSpaceName(space.getSpaceName());
        initeInfoVO.setUserName(loginUser.getUserName());
        initeInfoVO.setUserId(loginUser.getId());
        initeInfoVO.setSpaceId(spaceId);
        initeInfoVO.setExpireTime(LocalDateTime.now().plusMinutes(20));
        // 使用JSON序列化存储对象
        String jsonString = JSONUtil.toJsonStr(initeInfoVO);
        //存入redis,20分钟内有效
        valueOperations.set(string, jsonString, 20, TimeUnit.MINUTES);
        return string;
    }

    @Override
    public IniteInfoVO getInviteInfo(String inviteCode) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String inviteInfoStr = valueOperations.get(inviteCode.toString());
        // 使用JSON反序列化恢复对象
        IniteInfoVO initeInfoVO = JSONUtil.toBean(inviteInfoStr, IniteInfoVO.class);
        ThrowUtils.throwIf(initeInfoVO == null, StatusCode.PARAMS_ERROR, "邀请信息不存在/已失效");
        return initeInfoVO;
    }

    @Override
    public boolean acceptInvite(String inviteCode, User loginUser) {
        IniteInfoVO inviteInfo = this.getInviteInfo(inviteCode);
        Long spaceId = inviteInfo.getSpaceId();
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, StatusCode.PARAMS_ERROR, "空间不存在");
        LambdaQueryWrapper<SpaceUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SpaceUser::getSpaceId, spaceId);
        queryWrapper.eq(SpaceUser::getUserId, loginUser.getId());
        SpaceUser spaceUser = this.getOne(queryWrapper);
        ThrowUtils.throwIf(spaceUser != null, StatusCode.PARAMS_ERROR, "该用户已加入空间，请勿重复添加");
        spaceUser = new SpaceUser();
        spaceUser.setSpaceRole(SpaceRoleEnum.VIEWER.getValue());
        spaceUser.setSpaceId(spaceId);
        spaceUser.setUserId(loginUser.getId());
        boolean save = this.save(spaceUser);
        return save;
    }


}




