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
    @Autowired
    private RandomQueueName randomQueueName;
    private int retryLimit = 10;

    @PostConstruct
    public void consume() {
        try {
            start();
        } catch (JMSException e) {
            logger.error(e.getMessage(), e);
            if(retryLimit-- >= 0) {
                try {
                    logger.info("retry connect to active mq for {} times", retryLimit);
                    start();
                } catch (JMSException ex) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private void start() throws JMSException {
        String[] names = randomQueueName.queues();
        for(String name: names) {
            QueueConnection connection = factory.createQueueConnection();
            connection.start();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(name);
            MessageConsumer consumer = session.createConsumer(queue);
            consumer.setMessageListener(chatListner);
            logger.info("consumer ready for {}", name);
        }
    }

}
