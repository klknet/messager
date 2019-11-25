package com.konglk.ims.event;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.Constants;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.service.MessageService;
import com.konglk.ims.service.TopicNameManager;
import com.konglk.ims.util.SpringUtils;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

/**
 * Created by konglk on 2019/4/20.
 */
@Service
//@Profile("local")
public class TopicConsumer {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private TopicNameManager topicNameManager;
    @Autowired
    private MessageService messageService;
    @Autowired
    private SpringUtils springUtils;


    /**
     * 聊天消息监听处理
     * @param message
     */
    @JmsListener(destination = TopicNameManager.chat, containerFactory = "topicContainerFactory")
    public void consumeMessage(String message) {
        long ts = System.currentTimeMillis();
        String text = message;
        MessageDO messageDO = null;
        try {
            messageDO = JSON.parseObject(text, MessageDO.class);
            //消息处理事件
            messageService.notifyAll(messageDO, new Response(ResponseStatus.M_TRANSFER_MESSAGE, Response.MESSAGE, text));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        long cur = System.currentTimeMillis();
        //超过600ms的消息，记录为慢消费消息
        if (cur - ts > Constants.INTERVAL) {
            logger.warn("slow consume message. {}", cur - ts);
        }
    }

    @JmsListener(destination = TopicNameManager.notify, containerFactory = "topicContainerFactory")
    public void consumeNotify(String message) {
        String text = message;
        ResponseEvent event = null;
        try {
            event = JSON.parseObject(text, ResponseEvent.class);
            springUtils.getApplicationContext().publishEvent(event);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
