package com.oxn.aiPicturesStore.aop;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oxn.aiPicturesStore.annotation.AuthCheck;
import com.oxn.aiPicturesStore.annotation.AuthSpaceUserCheck;
import com.oxn.aiPicturesStore.constant.UserConstant;
import com.oxn.aiPicturesStore.enums.SpaceRoleEnum;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.enums.UserRoleEnum;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.mapper.SpaceUserMapper;
import com.oxn.aiPicturesStore.model.dto.spaceuser.SpaceUserQueryRequest;
import com.oxn.aiPicturesStore.model.dto.spaceuser.SpaceUserEditRequest;
import com.oxn.aiPicturesStore.model.entity.SpaceUser;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthSpaceUserInterceptor {

    @Autowired
    private SpaceUserMapper spaceUserMapper;

    @Autowired
    private UserService userService;

    @Around("@annotation(authSpaceUserCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthSpaceUserCheck authSpaceUserCheck) throws Throwable {
        String role = authSpaceUserCheck.mustRole();
        //获取当前用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        //判断当前用户是否有【当前】空间权限
        Object arg = joinPoint.getArgs()[0];
        Long spaceId = null;
        
        if (arg instanceof SpaceUserQueryRequest) {
            spaceId = ((SpaceUserQueryRequest) arg).getSpaceId();
        } else if (arg instanceof SpaceUserEditRequest) {
            spaceId = ((SpaceUserEditRequest) arg).getSpaceId();
        } else {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "参数类型不正确");
        }
        
        LambdaQueryWrapper<SpaceUser> spaceUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        spaceUserLambdaQueryWrapper.eq(SpaceUser::getUserId, loginUser.getId());
        spaceUserLambdaQueryWrapper.eq(SpaceUser::getSpaceId, spaceId);

        SpaceUser newSpaceUser = spaceUserMapper.selectOne(spaceUserLambdaQueryWrapper);
        if (newSpaceUser == null) {
            throw new BusinessException(StatusCode.OPERATION_ERROR, "未查到该用户");
        }
        if (!newSpaceUser.getSpaceRole().equals(role)) {
            throw new BusinessException(StatusCode.OPERATION_ERROR, "没有权限");
        }
        return joinPoint.proceed();
    }
}