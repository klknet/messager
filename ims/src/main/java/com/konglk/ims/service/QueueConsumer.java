package com.konglk.ims.service;

import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jms.*;

/**
 * Created by konglk on 2019/4/20.
 */
@Service
public class QueueConsumer implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource(name = "amqFactory")
    private PooledConnectionFactory factory;
    @Autowired
    private ChatListenerImpl chatListner;
    @Autowired
    private RandomQueueName randomQueueName;
    private QueueConnection connection;

    public void consume() {
        try {
            start();
        } catch (JMSException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void start() throws JMSException {
        String[] names = randomQueueName.queues();
        connection = factory.createQueueConnection();
        connection.start();
        for(int i=0; i<names.length; i++) {
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(names[i]);
            MessageConsumer consumer = session.createConsumer(queue);
            consumer.setMessageListener(chatListner);
            logger.info("consumer ready for {}", names[i]);
        }
    }

    @PreDestroy
    public void closeConnection() {
        if (connection != null) {
            try {
                System.out.println("close consumer queue"+connection.toString());
                connection.close();
            } catch (JMSException e) {
                logger.error("failed to release connection {}", connection.toString());
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        consume();
    }
}
