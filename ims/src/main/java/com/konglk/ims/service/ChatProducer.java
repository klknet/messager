package com.konglk.ims.service;

import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.*;

/**
 * Created by konglk on 2019/4/20.
 */
@Service
public class ChatProducer {
    @Resource(name = "amqFactory")
    private PooledConnectionFactory factory;
    @Autowired
    private RandomTopicName randomTopicName;

    public void send(String text, String route) {
        TopicConnection connection = null;
        try {
            connection = factory.createTopicConnection();
            connection.start();
            TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createTopic(randomTopicName.getTopicName(route)));
            TextMessage textMessage = session.createTextMessage(text);
            producer.send(textMessage);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
