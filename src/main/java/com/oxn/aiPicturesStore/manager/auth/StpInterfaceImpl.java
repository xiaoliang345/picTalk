package com.oxn.aiPicturesStore.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.oxn.aiPicturesStore.constant.UserConstant;
import com.oxn.aiPicturesStore.enums.SpaceRoleEnum;
import com.oxn.aiPicturesStore.enums.SpaceTypeEnum;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.manager.auth.model.SpaceUserPermissionConstant;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.SpaceUser;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.service.PictureService;
import com.oxn.aiPicturesStore.service.SpaceService;
import com.oxn.aiPicturesStore.service.SpaceUserService;
import com.oxn.aiPicturesStore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface {

    @Value("${server.servlet.context-path}")
    private String contextPath;
    
    @Autowired
    private SpaceUserAuthManager spaceUserAuthManager;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SpaceService spaceService;
    
    @Autowired
    private SpaceUserService spaceUserService;

    @Autowired
    private PictureService pictureService;

    /**
     * 返回一个账号所拥有的权限码集合
     */

    @Override
    public List<String> getRoleList(Object o, String s) {
        return null;
    }

   

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 判断 loginType，仅对类型为 "space" 进行权限校验
        if (!StpKit.SPACE_TYPE.equals(loginType)) {
            return new ArrayList<>();
        }
        
        // 获取管理员权限
        List<String> adminPermissions = getAdminPermissions();
        
        // 获取上下文对象
        SpaceUserAuthContext authContext = getAuthContextByRequest();
        
        // 如果所有字段都为空，表示查询公共图库，可以通过
        if (isAllFieldsNull(authContext)) {
            return adminPermissions;
        }
        
        // 获取登录用户
        User loginUser = getLoginUser(loginId);
        Long userId = loginUser.getId();
        
        // 优先从上下文中获取 SpaceUser 对象
        SpaceUser spaceUser = authContext.getSpaceUser();
        if (spaceUser != null) {
            return spaceUserAuthManager.getUserPermissions(spaceUser.getSpaceRole());
        }
        
        // 如果有 spaceUserId，通过 spaceUserId 获取权限
        Long spaceUserId = authContext.getSpaceUserId();
        if (spaceUserId != null) {
            return getPermissionsBySpaceUserId(spaceUserId, userId);
        }
        
        // 如果没有 spaceUserId，尝试通过 spaceId 或 pictureId 获取 Space 对象并处理
        Long spaceId = authContext.getSpaceId();
        if (spaceId == null) {
            // 通过 pictureId 获取权限或 spaceId
            PicturePermissionResult result = getPermissionOrSpaceIdByPictureId(
                    authContext, loginUser, userId, adminPermissions);
            if (result.getPermissions() != null) {
                // 已获得权限，直接返回
                return result.getPermissions();
            }
            // 如果没有获取到 spaceId（公共图库且有权限），返回管理员权限
            if (result.getSpaceId() == null) {
                return adminPermissions;
            }
            spaceId = result.getSpaceId();
        }
        
        // 根据 Space 对象获取权限
        return getPermissionsBySpaceId(spaceId, userId, loginUser, adminPermissions);
    }

    /**
     * 获取管理员权限列表
     */
    private List<String> getAdminPermissions() {
        return spaceUserAuthManager.getUserPermissions(SpaceRoleEnum.ADMIN.getValue());
    }

    /**
     * 获取登录用户信息
     */
    private User getLoginUser(Object loginId) {
        User loginUser = (User) StpKit.SPACE.getSessionByLoginId(loginId).get(UserConstant.USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(StatusCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        return loginUser;
    }

    /**
     * 通过 spaceUserId 获取权限
     */
    private List<String> getPermissionsBySpaceUserId(Long spaceUserId, Long userId) {
        SpaceUser spaceUser = spaceUserService.getById(spaceUserId);
        if (spaceUser == null) {
            throw new BusinessException(StatusCode.NOT_FOUND_ERROR, "未找到空间用户信息");
        }
        
        // 取出当前登录用户对应的 spaceUser
        SpaceUser loginSpaceUser = spaceUserService.lambdaQuery()
                .eq(SpaceUser::getSpaceId, spaceUser.getSpaceId())
                .eq(SpaceUser::getUserId, userId)
                .one();
        
        if (loginSpaceUser == null) {
            return new ArrayList<>();
        }
        
        return spaceUserAuthManager.getUserPermissions(loginSpaceUser.getSpaceRole());
    }

    /**
     * 通过 pictureId 获取权限或 spaceId
     * 如果已确定权限则返回权限，否则返回 spaceId 继续处理
     */
    private PicturePermissionResult getPermissionOrSpaceIdByPictureId(
            SpaceUserAuthContext authContext, User loginUser, 
            Long userId, List<String> adminPermissions) {
        Long pictureId = authContext.getPictureId();
        
        // 图片 id 也没有，则默认通过权限校验
        if (pictureId == null) {
            return new PicturePermissionResult(null, null);
        }
        
        Picture picture = pictureService.lambdaQuery()
                .eq(Picture::getId, pictureId)
                .select(Picture::getId, Picture::getSpaceId, Picture::getUserId)
                .one();
        
        if (picture == null) {
            throw new BusinessException(StatusCode.NOT_FOUND_ERROR, "未找到图片信息");
        }
        
        Long spaceId = picture.getSpaceId();
        
        // 公共图库，仅本人或管理员可操作
        if (spaceId == null) {
            if (picture.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
                // 公共图库且有权限，返回 null spaceId 表示需要返回管理员权限
                return new PicturePermissionResult(null, null);
            } else {
                // 不是自己的图片，仅可查看
                return new PicturePermissionResult(null, 
                        Collections.singletonList(SpaceUserPermissionConstant.PICTURE_VIEW));
            }
        }
        
        // 返回 spaceId，继续通过 Space 获取权限
        return new PicturePermissionResult(spaceId, null);
    }

    /**
     * 图片权限结果包装类
     */
    private static class PicturePermissionResult {
        private final Long spaceId;
        private final List<String> permissions;

        public PicturePermissionResult(Long spaceId, List<String> permissions) {
            this.spaceId = spaceId;
            this.permissions = permissions;
        }

        public Long getSpaceId() {
            return spaceId;
        }

        public List<String> getPermissions() {
            return permissions;
        }
    }

    /**
     * 通过 spaceId 获取权限
     */
    private List<String> getPermissionsBySpaceId(Long spaceId, Long userId, User loginUser, 
                                                  List<String> adminPermissions) {
        Space space = spaceService.getById(spaceId);
        if (space == null) {
            throw new BusinessException(StatusCode.NOT_FOUND_ERROR, "未找到空间信息");
        }
        
        // 根据 Space 类型判断权限
        if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
            return getPrivateSpacePermissions(space, userId, loginUser, adminPermissions);
        } else {
            return getTeamSpacePermissions(spaceId, userId);
        }
    }

    /**
     * 获取私有空间的权限
     */
    private List<String> getPrivateSpacePermissions(Space space, Long userId, User loginUser, 
                                                    List<String> adminPermissions) {
        // 私有空间，仅本人或管理员有权限
        if (space.getUserId().equals(userId) || userService.isAdmin(loginUser)) {
            return adminPermissions;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 获取团队空间的权限
     */
    private List<String> getTeamSpacePermissions(Long spaceId, Long userId) {
        SpaceUser spaceUser = spaceUserService.lambdaQuery()
                .eq(SpaceUser::getSpaceId, spaceId)
                .eq(SpaceUser::getUserId, userId)
                .one();
        
        if (spaceUser == null) {
            return new ArrayList<>();
        }
        
        return spaceUserAuthManager.getUserPermissions(spaceUser.getSpaceRole());
    }


    /**
     * 从请求中获取上下文对象
     */
    private SpaceUserAuthContext getAuthContextByRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String contentType = request.getHeader(Header.CONTENT_TYPE.getValue());
        SpaceUserAuthContext authContext;
        // JSON 请求
        if (ContentType.JSON.getValue().equals(contentType)) {
            String body = ServletUtil.getBody(request);
            authContext = JSONUtil.toBean(body, SpaceUserAuthContext.class);
        }
        // 表单请求
        else {
            Map<String, String> paramMap = ServletUtil.getParamMap(request);
            authContext = BeanUtil.toBean(paramMap, SpaceUserAuthContext.class);
        }
        Long id = authContext.getId();
        //获取请求的是哪个接口
        if (ObjUtil.isNotEmpty(id)) {
            String requestURI = request.getRequestURI();
            String firstSegment = StrUtil.split(requestURI, '/').get(2); // 获取第一个路径段
            switch (firstSegment) {
                case "space":
                    authContext.setSpaceId(id);
                    break;
                case "picture":
                    authContext.setPictureId(id);
                    break;
                case "spaceUser":
                    authContext.setSpaceUserId(id);
                    break;
            }
        }
        return authContext;
    }

    private boolean isAllFieldsNull(Object object) {
        if (object == null) {
            return true; // 对象本身为空
        }
        // 获取所有字段并判断是否所有字段都为空
        return Arrays.stream(ReflectUtil.getFields(object.getClass()))
                // 获取字段值
                .map(field -> ReflectUtil.getFieldValue(object, field))
                // 检查是否所有字段都为空
                .allMatch(ObjectUtil::isEmpty);
    }

}
