package com.oxn.aiPicturesStore.manager.websocket;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oxn.aiPicturesStore.enums.SpaceTypeEnum;
import com.oxn.aiPicturesStore.manager.auth.SpaceUserAuthContext;
import com.oxn.aiPicturesStore.manager.auth.SpaceUserAuthManager;
import com.oxn.aiPicturesStore.manager.auth.model.SpaceUserPermissionConstant;
import com.oxn.aiPicturesStore.mapper.SpaceUserMapper;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.Space;
import com.oxn.aiPicturesStore.model.entity.SpaceUser;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.service.PictureService;
import com.oxn.aiPicturesStore.service.SpaceService;
import com.oxn.aiPicturesStore.service.SpaceUserService;
import com.oxn.aiPicturesStore.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * websocket 拦截器
 */
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private PictureService pictureService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private SpaceUserAuthManager spaceUserAuthManager;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        //获取请求参数
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            //获取图片id
            String pictureId = servletRequest.getParameter("pictureId");
            if (StrUtil.isBlank(pictureId)) {
                log.info("pictureId不能为空,拒绝连接");
                return false;
            }
            //获取图片
            Picture picture = pictureService.getById(pictureId);
            if (picture == null) {
                log.info("图片不存在,拒绝连接");
                return false;
            }
            //获取当前用户
            User loginUser = userService.getLoginUser(servletRequest);
            if (loginUser == null) {
                log.info("用户未登录,拒绝连接");
                return false;
            }
            //获取空间 id
            Long spaceId = picture.getSpaceId();
            if (spaceId != null) {
                Space space = spaceService.getById(spaceId);
                if (space == null) {
                    log.info("空间不存在,拒绝连接");
                    return false;
                }
                if (space.getSpaceType() == SpaceTypeEnum.PRIVATE.getValue()) {
                    log.info("私有空间不支持协同编辑,拒绝连接");
                    return false;
                }
                //判断当前用户是否有权限
                List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
                if (!permissionList.contains(SpaceUserPermissionConstant.PICTURE_EDIT)) {
                    log.info("当前用户无权限,拒绝连接");
                    return false;
                }
                //存储信息到websocket会话中
                attributes.put("user", loginUser);
                attributes.put("userId", loginUser.getId());
                attributes.put("pictureId", Long.valueOf(pictureId));
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
