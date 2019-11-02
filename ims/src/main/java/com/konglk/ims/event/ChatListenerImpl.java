package com.konglk.ims.event;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.Constants;
import com.konglk.ims.domain.FailedMessageDO;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.service.MessageService;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Date;

/**
 * Created by konglk on 2019/4/20.
 * 接收聊天消息
 */
@Service
public class ChatListenerImpl implements MessageListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MessageService messageService;


    @Override
    public void onMessage(Message message) {
        if(message instanceof ActiveMQTextMessage) {
            long ts = System.currentTimeMillis();
            ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;
            MessageDO messageDO = null;
            String text = null;
            try {
                text = textMessage.getText();
                messageDO = JSON.parseObject(text, MessageDO.class);
                //消息处理事件
                messageService.notifyAll(messageDO, new Response(ResponseStatus.M_TRANSFER_MESSAGE, Response.MESSAGE, text));
            }catch(JMSException jms) {
                logger.error(jms.getMessage(), jms);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            long cur = System.currentTimeMillis();
            //超过600ms的消息，记录为慢消费消息
            if (cur - ts > Constants.INTERVAL) {
                logger.warn("slow consume message. {}", cur - ts);
            }
        }
    }




}
