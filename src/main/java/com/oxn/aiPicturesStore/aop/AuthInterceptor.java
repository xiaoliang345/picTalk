package com.oxn.aiPicturesStore.aop;

import com.oxn.aiPicturesStore.annotation.AuthCheck;
import com.oxn.aiPicturesStore.constant.UserConstant;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.enums.UserRoleEnum;
import com.oxn.aiPicturesStore.exception.BusinessException;
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
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Autowired
    private UserService userService;

    @Around("@annotation(authcheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint,AuthCheck authcheck) throws Throwable {
        String role = authcheck.mustRole();
        //获取当前用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(StatusCode.NOT_LOGIN_ERROR);
        }
        UserRoleEnum enumByValue = UserRoleEnum.getEnumByValue(role);
        //不需要认证
        if(enumByValue==null){
            return joinPoint.proceed();
        }
        //需要管理员权限，但登录用户不是管理员
        if(!loginUser.getUserRole().equals(role)){
            throw new BusinessException(StatusCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
