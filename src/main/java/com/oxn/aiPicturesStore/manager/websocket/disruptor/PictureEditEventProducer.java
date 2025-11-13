package com.oxn.aiPicturesStore.manager.websocket.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.oxn.aiPicturesStore.manager.websocket.model.PictureEditRequestMessage;
import com.oxn.aiPicturesStore.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Component
@Slf4j
public class PictureEditEventProducer {

    @Resource
    private Disruptor<PictureEditEvent> pictureEditEventDisruptor;

    public void publishEvent(Long pictureId, User user, WebSocketSession session, PictureEditRequestMessage pictureEditRequestMessage) {
        RingBuffer<PictureEditEvent> ringBuffer = pictureEditEventDisruptor.getRingBuffer();
        long next = ringBuffer.next();
        try {
            PictureEditEvent pictureEditEvent = ringBuffer.get(next);
            pictureEditEvent.setPictureId(pictureId);
            pictureEditEvent.setUser(user);
            pictureEditEvent.setSession(session);
            pictureEditEvent.setPictureEditRequestMessage(pictureEditRequestMessage);
        } finally {
            ringBuffer.publish(next);
        }

    }

    @PreDestroy
    public void close(){
        pictureEditEventDisruptor.shutdown();
    }

}
