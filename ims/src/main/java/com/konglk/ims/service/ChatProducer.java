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
    private RandomQueueName randomQueueName;

    public void send(String text) {
        QueueConnection connection = null;
        try {
            connection = factory.createQueueConnection();
            connection.start();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createQueue(randomQueueName.getQueueName()));
            TextMessage textMessage = session.createTextMessage(text);
            producer.send(textMessage);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
