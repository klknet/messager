package com.konglk.ims.event;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.service.TopicNameManager;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.*;

/**
 * Created by konglk on 2019/4/20.
 */
@Service
public class TopicProducer {
    @Resource(name = "amqFactory")
    private PooledConnectionFactory factory;
    @Autowired
    private TopicNameManager topicNameManager;

    /**
     * 发送聊天topic
     * @param text
     * @param route
     */
    public void sendChatMessage(String text, String route) {
        TopicConnection connection = null;
        try {
            connection = factory.createTopicConnection();
            connection.start();
            TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createTopic(topicNameManager.getChatName(route)));
            TextMessage textMessage = session.createTextMessage(text);
            producer.send(textMessage);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知类topic
     * @param text
     */
    public void sendNotifyMessage(ResponseEvent event) {
        TopicConnection connection = null;
        try {
            connection = factory.createTopicConnection();
            connection.start();
            TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createTopic(topicNameManager.getNotifyName()));
            TextMessage textMessage = session.createTextMessage(JSON.toJSONString(event));
            producer.send(textMessage);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
