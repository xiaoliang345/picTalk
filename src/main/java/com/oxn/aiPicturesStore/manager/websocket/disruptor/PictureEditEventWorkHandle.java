package com.oxn.aiPicturesStore.manager.websocket.disruptor;

import com.lmax.disruptor.WorkHandler;
import com.oxn.aiPicturesStore.manager.websocket.PictureEditHandler;
import com.oxn.aiPicturesStore.manager.websocket.model.PictureEditMessageTypeEnum;
import com.oxn.aiPicturesStore.manager.websocket.model.PictureEditRequestMessage;
import com.oxn.aiPicturesStore.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@Slf4j
public class PictureEditEventWorkHandle implements WorkHandler<PictureEditEvent> {

    @Autowired
    private PictureEditHandler pictureEditHandler;

    @Override
    public void onEvent(PictureEditEvent pictureEditEvent) throws Exception {
        PictureEditRequestMessage pictureEditRequestMessage = pictureEditEvent.getPictureEditRequestMessage();
        String type = pictureEditRequestMessage.getType();
        PictureEditMessageTypeEnum enumByValue = PictureEditMessageTypeEnum.getEnumByValue(type);
        Long pictureId = pictureEditEvent.getPictureId();
        User user = pictureEditEvent.getUser();
        WebSocketSession session = pictureEditEvent.getSession();
        switch (enumByValue) {
            case EDIT_ACTION:
                pictureEditHandler.handleEditAction(pictureId, user, session, pictureEditRequestMessage);
                break;
            case ENTER_EDIT:
                pictureEditHandler.handleEnterEdit(pictureId, user, session, pictureEditRequestMessage);
                break;
            case EXIT_EDIT:
                pictureEditHandler.handleExitEdit(pictureId, user, session, pictureEditRequestMessage);
                break;
            default:
                break;
        }
    }
}
