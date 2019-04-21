package com.konglk.ims.service;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jms.*;

/**
 * Created by konglk on 2019/4/20.
 */
@Service
public class QueueConsumer {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource(name = "amqFactory")
    private ActiveMQConnectionFactory factory;
    @Autowired
    private ChatListenerImpl chatListner;
    public static String chatQueue = "ims_chat";
    private int retryLimit = 10;

    @PostConstruct
    public void consume() {
        QueueConnection connection = null;
        try {
            start(connection);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if(connection != null) {
                try {
                    connection.close();
                } catch (JMSException ex) {
                }
            }
            if(retryLimit-- >= 0) {
                try {
                    start(connection);
                } catch (JMSException ex) {
                }
            }
        }
    }

    private void start(QueueConnection connection) throws JMSException {
        connection = factory.createQueueConnection();
        connection.start();
        QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(chatQueue);
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(chatListner);
        logger.info("consumer ready");
    }
}
