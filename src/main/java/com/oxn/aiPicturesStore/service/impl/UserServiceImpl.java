package com.oxn.aiPicturesStore.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.server.HttpServerRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxn.aiPicturesStore.constant.UserConstant;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.UserLoginVo;
import com.oxn.aiPicturesStore.service.UserService;
import com.oxn.aiPicturesStore.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
* @author 34576
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-09-21 19:51:56
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @return
     */
    @Override
    public UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.数据校验
        if(userAccount==null||userPassword==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"参数不能为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"账号过短");
        }
        if(userPassword.length()<8){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"密码过短");
        }
        //2.密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        //3.查询用户
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount",userAccount);
        userQueryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(userQueryWrapper);
        ThrowUtils.throwIf(user==null,StatusCode.PARAMS_ERROR,"账号或密码错误");
        //4.返回用户信息
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE,user);
        UserLoginVo userLoginVo = new UserLoginVo();
        BeanUtil.copyProperties(user,userLoginVo);
        userLoginVo.setUserAvatar("https://c-ssl.duitang.com/uploads/blog/202503/10/OoSP1wybF6YdPge.jpeg");
        return userLoginVo;
    }

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.数据校验
        if(userAccount==null||userPassword==null||checkPassword==null){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"参数不能为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"账号过短");
        }
        if(userPassword.length()<8){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"密码过短");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"密码不一致");
        }
        //2.判断是否已创建
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount",userAccount);
        Long l = userMapper.selectCount(userQueryWrapper);
        if(l>0){
            throw new BusinessException(StatusCode.PARAMS_ERROR,"用户已存在");
        }
        //3.密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        //4.创建用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("萌新");
        int insert = userMapper.insert(user);
        if(insert<=0){
            throw new BusinessException(StatusCode.SYSTEM_ERROR,"注册失败,系统错误");
        }
        return user.getId();
    }

    /**
     * 获取加密密码
     * @param userPassword
     * @return
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        final String SALT = "ai_pictures_store_oxn";
        return DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
    }
}




