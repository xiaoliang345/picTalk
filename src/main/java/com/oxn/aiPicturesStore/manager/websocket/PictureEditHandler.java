package com.oxn.aiPicturesStore.manager.websocket;

import cn.hutool.json.JSONUtil;
import com.oxn.aiPicturesStore.manager.websocket.disruptor.PictureEditEventProducer;
import com.oxn.aiPicturesStore.manager.websocket.model.PictureEditActionEnum;
import com.oxn.aiPicturesStore.manager.websocket.model.PictureEditMessageTypeEnum;
import com.oxn.aiPicturesStore.manager.websocket.model.PictureEditRequestMessage;
import com.oxn.aiPicturesStore.manager.websocket.model.PictureEditResponseMessage;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.UserVO;
import com.oxn.aiPicturesStore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PictureEditHandler extends TextWebSocketHandler {

    @Autowired
    private UserService userService;

    @Resource
    @Lazy
    private PictureEditEventProducer pictureEditEventProducer;

    // 每张图片的编辑状态，key: pictureId, value: 当前正在编辑的用户 ID
    private final Map<Long, Long> pictureEditingUsers = new ConcurrentHashMap<>();

    // 保存所有连接的会话，key: pictureId, value: 用户会话集合
    private final Map<Long, Set<WebSocketSession>> pictureSessions = new ConcurrentHashMap<>();

    /**
     * 连接建立成功
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 保存会话
        if(ObjectUtils.isEmpty(pictureSessions.get(pictureId))){
            pictureSessions.put(pictureId, ConcurrentHashMap.newKeySet());
        }
        pictureSessions.get(pictureId).add(session);
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        pictureEditResponseMessage.setMessage(String.format("用户%s加入编辑", user.getUserName()));
        pictureEditResponseMessage.setUser(userService.getUserVo(user));
        // 推送给所有用户
        broadcast(pictureId, pictureEditResponseMessage, session);
    }

    /**
     * 收到消息
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        PictureEditRequestMessage pictureEditRequestMessage = JSONUtil.toBean(message.getPayload(), PictureEditRequestMessage.class);
        //处理收到的消息
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum enumByValue = PictureEditMessageTypeEnum.getEnumByValue(type);

        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        // 处理消息
        pictureEditEventProducer.publishEvent(pictureId, user, session, pictureEditRequestMessage);
    }

    /**
     * 退出编辑
     *
     * @param pictureId
     * @param user
     * @param session
     * @param pictureEditRequestMessage
     */
    public void handleExitEdit(Long pictureId, User user, WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage) {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            // 删除会话
            pictureEditingUsers.remove(pictureId);
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EXIT_EDIT.getValue());
            pictureEditResponseMessage.setMessage(String.format("%s退出", user.getUserName()));
            pictureEditResponseMessage.setUser(userService.getUserVo(user));
            // 推送给所有用户
            broadcast(pictureId, pictureEditResponseMessage, session);
        }
    }

    /**
     * 进入编辑
     *
     * @param pictureId
     * @param user
     * @param session
     * @param pictureEditRequestMessage
     */
    public void handleEnterEdit(Long pictureId, User user, WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage) {
        //如果没有用户编辑则进入编辑
        if (!pictureEditingUsers.containsKey(pictureId)) {
            pictureEditingUsers.put(pictureId, user.getId());
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.ENTER_EDIT.getValue());
            pictureEditResponseMessage.setMessage(String.format("用户%s正在编辑", user.getUserName()));
            pictureEditResponseMessage.setUser(userService.getUserVo(user));
            // 推送给所有用户
            broadcast(pictureId, pictureEditResponseMessage, session);
        }
    }

    /**
     * 编辑操作
     *
     * @param pictureId
     * @param user
     * @param session
     * @param pictureEditRequestMessage
     */
    public void handleEditAction(Long pictureId, User user, WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage) {
        Long editingUserId = pictureEditingUsers.get(pictureId);
        String editAction = pictureEditRequestMessage.getEditAction();
        PictureEditActionEnum enumByValue = PictureEditActionEnum.getEnumByValue(editAction);
        if (editingUserId != null && editingUserId.equals(user.getId())) {
            PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
            pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.EDIT_ACTION.getValue());
            pictureEditResponseMessage.setMessage(String.format("%s执行%s", user.getUserName(), enumByValue.getText()));
            pictureEditResponseMessage.setUser(userService.getUserVo(user));
            pictureEditResponseMessage.setEditAction(enumByValue.getValue());
            // 推送给所有用户
            broadcast(pictureId, pictureEditResponseMessage, session);
        }
    }

    /**
     * 连接关闭
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        User user = (User) session.getAttributes().get("user");
        Long pictureId = (Long) session.getAttributes().get("pictureId");
        this.handleExitEdit(pictureId, user, session, null);
        pictureEditingUsers.remove(pictureId);
        Set<WebSocketSession> webSocketSessions = pictureSessions.get(pictureId);
        if (webSocketSessions != null) {
            webSocketSessions.remove(session);
            if (webSocketSessions.isEmpty()) {
                pictureSessions.remove(pictureId);
            }
        }
        PictureEditResponseMessage pictureEditResponseMessage = new PictureEditResponseMessage();
        pictureEditResponseMessage.setType(PictureEditMessageTypeEnum.INFO.getValue());
        pictureEditResponseMessage.setMessage(String.format("%s 离开", user.getUserName()));
        pictureEditResponseMessage.setUser(userService.getUserVo(user));
    }

    /**
     * 广播消息
     *
     * @param pictureId
     * @param message
     * @param webSocketSession
     */
    private void broadcast(Long pictureId, PictureEditResponseMessage message, WebSocketSession webSocketSession) {
        Set<WebSocketSession> sessions = pictureSessions.get(pictureId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                try {
                    if (session.isOpen() ) {
                        session.sendMessage(new TextMessage(JSONUtil.toJsonStr(message)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
