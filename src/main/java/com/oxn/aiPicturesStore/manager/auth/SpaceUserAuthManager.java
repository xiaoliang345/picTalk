package com.oxn.aiPicturesStore.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.oxn.aiPicturesStore.enums.SpaceRoleEnum;
import com.oxn.aiPicturesStore.enums.SpaceTypeEnum;
import com.oxn.aiPicturesStore.manager.auth.model.SpaceUserAuthConfig;
import com.oxn.aiPicturesStore.manager.auth.model.SpaceUserPermissionConstant;
import com.oxn.aiPicturesStore.manager.auth.model.SpaceUserRole;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.SpaceUser;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.service.SpaceUserService;
import com.oxn.aiPicturesStore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class SpaceUserAuthManager {

    @Autowired
    private UserService userService;

    @Autowired
    private SpaceUserService spaceUserService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH;

    static {
        String s = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH = JSONUtil.toBean(s, SpaceUserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     * @param userRole
     * @return
     */
    public List<String> getUserPermissions(String userRole) {
        if (userRole == null) {
            return null;
        }
        for (SpaceUserRole role : SPACE_USER_AUTH.getRoles()) {
            if (role.getKey().equals(userRole)) {
                return role.getPermissions();
            }
        }
        return null;
    }

    /**
     * 根据空间和角色获取权限列表
     * @param space
     * @param loginUser
     * @return
     */
    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = this.getUserPermissions(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW);
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return this.getUserPermissions(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }

}
