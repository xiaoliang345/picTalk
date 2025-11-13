package com.oxn.aiPicturesStore.manager.websocket.disruptor;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.ThreadFactory;

@Configuration
public class PictureEditEventDisruptorConfig {
    @Resource
    private PictureEditEventWorkHandle pictureEditEventWorkHandle;

    @Bean("pictureEditEventDisruptor")
    public Disruptor<PictureEditEvent> init() {
        //队列大小，必须是2的幂次方
        int bufferSize = 1024 * 16; // 改为16384（2^14），确保是2的幂次方
        Disruptor<PictureEditEvent> disruptor = new Disruptor<>(
                PictureEditEvent::new,
                bufferSize,
                ThreadFactoryBuilder.create().setNamePrefix("pictureEditEventDisruptor-").build()
        );
        //处理器
        disruptor.handleEventsWithWorkerPool(pictureEditEventWorkHandle);
        disruptor.start();
        return disruptor;
    }
}