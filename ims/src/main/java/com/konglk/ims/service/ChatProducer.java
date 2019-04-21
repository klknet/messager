package com.konglk.ims.service;

import com.konglk.ims.domain.MessageDO;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jms.*;

/**
 * Created by konglk on 2019/4/20.
 */
@Service
public class ChatProducer {
    @Resource(name = "amqFactory")
    private ActiveMQConnectionFactory factory;

    public void send(String text) {
        QueueConnection connection = null;
        try {
            connection = factory.createQueueConnection();
            connection.start();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(session.createQueue(QueueConsumer.chatQueue));
            TextMessage textMessage = session.createTextMessage(text);
            producer.send(textMessage);
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
