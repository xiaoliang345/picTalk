package com.oxn.aiPicturesStore.manager.websocket.disruptor;

import com.oxn.aiPicturesStore.manager.websocket.model.PictureEditRequestMessage;
import com.oxn.aiPicturesStore.model.entity.User;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

/**
 * 图片编辑事件
 */
@Data
public class PictureEditEvent {
    /**
     * 图片ID
     */
    Long pictureId;
    /**
     * 用户
     */
    User user;
    /**
     * 会话
     */
    WebSocketSession session;
    /**
     * 请求消息
     */
    PictureEditRequestMessage pictureEditRequestMessage;
}
