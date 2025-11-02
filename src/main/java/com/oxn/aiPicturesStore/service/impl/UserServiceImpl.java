package com.oxn.aiPicturesStore.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.server.HttpServerRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxn.aiPicturesStore.constant.UserConstant;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.manager.auth.StpKit;
import com.oxn.aiPicturesStore.model.dto.user.UserQueryRequest;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.UserLoginVo;
import com.oxn.aiPicturesStore.model.vo.UserVO;
import com.oxn.aiPicturesStore.service.UserService;
import com.oxn.aiPicturesStore.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 34576
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-09-21 19:51:56
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @return
     */
    @Override
    public UserLoginVo userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.数据校验
        if (userAccount == null || userPassword == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "密码过短");
        }
        //2.密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        //3.查询用户
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        userQueryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(userQueryWrapper);
        ThrowUtils.throwIf(user == null, StatusCode.PARAMS_ERROR, "账号或密码错误");
        //4.返回用户信息
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 3. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        // 4. 记录用户登录态到 Sa-token，便于空间鉴权时使用，注意保证该用户信息与 SpringSession 中的信息过期时间一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);

        UserLoginVo userLoginVo = new UserLoginVo();
        BeanUtil.copyProperties(user, userLoginVo);
        return userLoginVo;
    }

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.数据校验
        if (userAccount == null || userPassword == null || checkPassword == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "密码不一致");
        }
        //2.判断是否已创建
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        Long l = userMapper.selectCount(userQueryWrapper);
        if (l > 0) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "用户已存在");
        }
        //3.密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        //4.创建用户
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserAvatar("https://img.itouxiang.com/m12/13/9a/19cff7d61987.jpg");
        user.setUserName("萌新");
        int insert = userMapper.insert(user);
        if (insert <= 0) {
            throw new BusinessException(StatusCode.SYSTEM_ERROR, "注册失败,系统错误");
        }
        return user.getId();
    }


    /**
     * 获取加密密码
     *
     * @param userPassword
     * @return
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        final String SALT = "ai_pictures_store_oxn";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 获取登录用户信息(管理员使用)
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object obj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) obj;
        if (user == null || user.getId() == null) {
            throw new BusinessException(StatusCode.NOT_LOGIN_ERROR);
        }
        Long id = user.getId();
        User loginUser = this.getById(id);
        if (loginUser == null) {
            throw new BusinessException(StatusCode.NOT_LOGIN_ERROR);
        }
        return loginUser;
    }

    /**
     * 信息脱敏
     *
     * @param user
     * @return
     */
    @Override
    public UserLoginVo getUserLoginVo(User user) {
        if (user == null) {
            return null;
        }
        UserLoginVo userLoginVo = new UserLoginVo();
        BeanUtil.copyProperties(user, userLoginVo);
        return userLoginVo;
    }

    /**
     * 用户注册
     *
     * @param request
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object obj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) obj;
        if (user == null || user.getId() == null) {
            throw new BusinessException(StatusCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取用户信息脱敏
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVo(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取用户信息列表脱敏
     *
     * @param list
     * @return
     */
    @Override
    public List<UserVO> getUserVoList(List<User> list) {
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream().map(this::getUserVo).collect(Collectors.toList());
    }

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null && id != 0L, "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public Boolean isAdmin(User user) {
        if (user == null) return false;
        return UserConstant.USER_ROLE_ADMIN.equals(user.getUserRole());
    }


}




