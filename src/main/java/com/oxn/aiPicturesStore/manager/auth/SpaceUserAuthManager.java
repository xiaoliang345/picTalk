package com.oxn.aiPicturesStore.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.oxn.aiPicturesStore.manager.auth.model.SpaceUserAuthConfig;
import com.oxn.aiPicturesStore.manager.auth.model.SpaceUserRole;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.util.List;

@Component
public class SpaceUserAuthManager {
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
}
