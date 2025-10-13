package com.oxn.aiPicturesStore.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxn.aiPicturesStore.enums.SpaceLevelEnum;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.model.dto.space.SpaceAddRequest;
import com.oxn.aiPicturesStore.model.dto.space.SpaceQueryRequest;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.PictureVO;
import com.oxn.aiPicturesStore.model.vo.SpaceVO;
import com.oxn.aiPicturesStore.model.vo.UserVO;
import com.oxn.aiPicturesStore.service.SpaceService;
import com.oxn.aiPicturesStore.mapper.SpaceMapper;
import com.oxn.aiPicturesStore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
* @author 34576
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-10-10 20:26:22
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService{
    
    @Autowired
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    // 定义锁池（全局唯一，通常作为类的静态成员）
    private static final Map<Long, Object> LOCK_MAP = new ConcurrentHashMap<>();

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest,space);
        //1参数填充
        Integer spaceLevel = space.getSpaceLevel();
        String spaceName = space.getSpaceName();
        if(spaceName==null){
            space.setSpaceName("默认空间");
        }
        if(spaceLevel==null){
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        Long userId = loginUser.getId();
        space.setUserId(userId);
        this.fillSpaceBySpaceLevel(space);
        //2参数校验
        this.validSpace(space,true);
        //3校验权限（普通用户只能创建普通）
        if(SpaceLevelEnum.COMMON.getValue()!=space.getSpaceLevel()&&userService.isAdmin(loginUser)){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"创建空间级别错误");
        }
        //4同一用户只能创建一个私有空间
        Object lock = LOCK_MAP.computeIfAbsent(userId, k -> new Object());
        synchronized (lock){
            try{
                Long execute = transactionTemplate.execute(status -> {
                    boolean exists = this.lambdaQuery()
                            .eq(Space::getUserId, userId)
                            .exists();
                    ThrowUtils.throwIf(exists, StatusCode.OPERATION_ERROR, "不能创建多个个人空间");
                    boolean save = this.save(space);
                    ThrowUtils.throwIf(!save, StatusCode.OPERATION_ERROR, "创建失败");
                    return space.getId();
                });
                return execute;
            }finally {
                LOCK_MAP.remove(lock);
            }
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {

        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String spaceName = spaceQueryRequest.getSpaceName();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(id) && !id.equals(0L), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId) && !userId.equals(0L), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel) && !spaceLevel.equals(0L), "spaceLevel", spaceLevel);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest httpServletRequest) {
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        Long userId = space.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVo = userService.getUserVo(user);
            spaceVO.setUser(userVo);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVO(Page<Space> spacePage, HttpServletRequest request) {
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(),
                spacePage.getSize(), spacePage.getTotal());
        List<Space> records = spacePage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return spaceVOPage;
        }
        //封装对象列表
        List<SpaceVO> spaceVOList = records.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        Set<Long> userIdSet = records.stream().map(Space::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userMap = userService.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userMap.containsKey(userId)) {
                user = userMap.get(userId).get(0);
                spaceVO.setUser(userService.getUserVo(user));
            }
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public void validSpace(Space space,Boolean add) {
        Integer spaceLevel = space.getSpaceLevel();
        String spaceName = space.getSpaceName();
        if(StrUtil.isBlank(spaceName)||spaceName.length()>=20){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"空间名称错误");
        }
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);
        if(enumByValue==null||spaceLevel==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"空间类型错误");
        }
    }

    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        ThrowUtils.throwIf(space==null,StatusCode.PARAMS_ERROR);
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum enumByValue = SpaceLevelEnum.getEnumByValue(spaceLevel);
        Integer maxCount = enumByValue.getMaxCount();
        long maxSize = enumByValue.getMaxSize();
        if(space.getMaxCount()==null)space.setMaxCount(Long.valueOf(maxCount));
        if(space.getMaxSize()==null)space.setMaxSize(Long.valueOf(maxSize));
    }
}




