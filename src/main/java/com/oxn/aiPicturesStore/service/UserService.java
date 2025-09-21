package com.oxn.aiPicturesStore.service;

import cn.hutool.http.server.HttpServerRequest;
import com.oxn.aiPicturesStore.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxn.aiPicturesStore.model.vo.UserLoginVo;

import javax.servlet.http.HttpServletRequest;

/**
* @author 34576
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-09-21 19:51:56
*/
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取加密密码
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);
}
