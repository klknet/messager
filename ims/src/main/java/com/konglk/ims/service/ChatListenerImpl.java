package com.konglk.ims.service;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.ws.ChatClient;
import com.konglk.ims.ws.ChatEndPoint;
import com.konglk.model.Response;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Created by konglk on 2019/4/20.
 */
@Service
public class ChatListenerImpl implements MessageListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MessageService messageService;
    @Autowired
    private ChatClient chatClient;

    @Override
    public void onMessage(Message message) {
        if(message instanceof ActiveMQTextMessage) {
            ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;
            try {
                String text = textMessage.getText();
                MessageDO messageDO = JSON.parseObject(text, MessageDO.class);
                messageService.insert(messageDO);
                String destId = messageDO.getDestId();
                ChatEndPoint client = chatClient.getClient(destId);
                if(client == null)
                    return;
                Response resp = new Response();
                resp.setCode(200);
                resp.setData(text);
                resp.setType(2);
                client.getSession().getBasicRemote().sendText(JSON.toJSONString(resp));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
