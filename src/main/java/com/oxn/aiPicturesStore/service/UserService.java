package com.oxn.aiPicturesStore.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oxn.aiPicturesStore.model.dto.user.UserQueryRequest;
import com.oxn.aiPicturesStore.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxn.aiPicturesStore.model.vo.UserLoginVo;
import com.oxn.aiPicturesStore.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取登录用户信息(脱敏)
     * @param user
     * @return
     */
    UserLoginVo getUserLoginVo(User user);

    /**
     * 用户注册
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取用户信息(脱敏)
     * @param user
     * @return
     */
    UserVO getUserVo(User user);

    /**
     * 获取用户信息列表(脱敏)
     *
     * @param list
     * @return
     */
    List<UserVO> getUserVoList(List<User> list);

    /**
     * 获取查询包装类
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    Boolean isAdmin(User user);
}
