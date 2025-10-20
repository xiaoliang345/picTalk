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
import com.oxn.aiPicturesStore.mapper.SpaceMapper;
import com.oxn.aiPicturesStore.model.dto.space.SpaceAddRequest;
import com.oxn.aiPicturesStore.model.dto.space.SpaceQueryRequest;
import com.oxn.aiPicturesStore.model.dto.space.analyze.SpaceAnalyzeRequest;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.SpaceVO;
import com.oxn.aiPicturesStore.model.vo.UserVO;
import com.oxn.aiPicturesStore.service.SpaceAnalyzeService;
import com.oxn.aiPicturesStore.service.SpaceService;
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
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceAnalyzeService {

    @Autowired
    private UserService userService;

    @Autowired
    private SpaceService spaceService;

    public void checkSpaceAnalyzeAuch(SpaceAnalyzeRequest spaceAnalyzeRequest,User loginUser){
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        if(queryAll||queryPublic){
           ThrowUtils.throwIf(!userService.isAdmin(loginUser),StatusCode.NO_AUTH_ERROR,"没有权限");
        }
        else{
            ThrowUtils.throwIf(spaceId==null,StatusCode.PARAMS_ERROR,"空间id为空");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space==null,StatusCode.PARAMS_ERROR,"空间不存在");
            spaceService.chechUserHasAuth(loginUser,space);
        }
    }

    @Override
    public void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        boolean queryAll = spaceAnalyzeRequest.isQueryAll();
        boolean queryPublic = spaceAnalyzeRequest.isQueryPublic();
        if(queryAll){
            return ;
        }
        if(queryPublic){
            queryWrapper.isNull("spaceId");
            return ;
        }
        if(spaceId!=null){
            queryWrapper.eq("spaceId",spaceId);
            return ;
        }
        throw new BusinessException(StatusCode.PARAMS_ERROR,"未指定查询范围");
    }

}




