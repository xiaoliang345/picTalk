package com.oxn.aiPicturesStore.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oxn.aiPicturesStore.annotation.AuthCheck;
import com.oxn.aiPicturesStore.common.BaseResponse;
import com.oxn.aiPicturesStore.common.DeleteRequest;
import com.oxn.aiPicturesStore.common.ResultUtils;
import com.oxn.aiPicturesStore.constant.UserConstant;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.model.dto.user.*;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.UserLoginVo;
import com.oxn.aiPicturesStore.model.vo.UserVO;
import com.oxn.aiPicturesStore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<UserLoginVo> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, StatusCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        UserLoginVo userLoginVo = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(userLoginVo);
    }

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, StatusCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long n = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(n);
    }

    /**
     * 获取登录用户信息
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<UserLoginVo> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getUserLoginVo(user));
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(request));
    }

    /**
     * 添加用户
     *
     * @param userAddRequest
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(StatusCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        //设置默认密码
        String encryptPassword = userService.getEncryptPassword("12345678");
        user.setUserPassword(encryptPassword);
        user.setUserAvatar("https://img.itouxiang.com/m12/13/9a/19cff7d61987.jpg");
        if (StringUtils.isEmpty(user.getUserRole())) user.setUserRole(UserConstant.USER_ROLE_USER);
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save, StatusCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据ID获取用户（管理员）
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<User> getUserById(Long id) {
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, StatusCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据ID获取用户
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(Long id) {
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, StatusCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVo(user));
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, StatusCode.PARAMS_ERROR);
        boolean delete = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(delete);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null, StatusCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean update = userService.updateById(user);
        ThrowUtils.throwIf(!update, StatusCode.OPERATION_ERROR);
        return ResultUtils.success(update);
    }

    /**
     * 获取用户分页列表（管理员）
     *
     * @param userQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.USER_ROLE_ADMIN)
    public BaseResponse<Page<UserVO>>listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, StatusCode.PARAMS_ERROR);
        Page<User> page = userService.page(new Page<>(userQueryRequest.getCurrent(), userQueryRequest.getPageSize()),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage =new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<UserVO> userVoList = userService.getUserVoList(page.getRecords());
        userVOPage.setRecords(userVoList);
        return ResultUtils.success(userVOPage);
    }
}
