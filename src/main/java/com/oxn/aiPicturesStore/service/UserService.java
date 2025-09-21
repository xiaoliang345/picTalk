package com.oxn.aiPicturesStore.service;

import com.oxn.aiPicturesStore.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 34576
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-09-21 19:51:56
*/
public interface UserService extends IService<User> {

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
