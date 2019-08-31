package com.konglk.ims.event;

import com.konglk.ims.service.TopicNameManager;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class TopicConsumer {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource(name = "amqFactory")
    private PooledConnectionFactory factory;
    @Autowired
    private ChatListenerImpl chatListener;
    @Autowired
    private NotifyListenerImpl notifyListener;
    @Autowired
    private TopicNameManager topicNameManager;
    private TopicConnection connection;

    @PostConstruct
    public void consume() {
        try {
            start();
        } catch (JMSException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void start() throws JMSException {
        String[] names = topicNameManager.topics();
        connection = factory.createTopicConnection();
        connection.start();
        //聊天消息topic
        for(int i=0; i<names.length; i++) {
            TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(names[i]);
            MessageConsumer consumer = session.createConsumer(topic);
            consumer.setMessageListener(chatListener);
            logger.info("consumer ready for {}", names[i]);
        }
        //通知类topic
        TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicNameManager.getNotifyName());
        MessageConsumer consumer = session.createConsumer(topic);
        consumer.setMessageListener(notifyListener);
        logger.info("consumer ready for {}", topicNameManager.getNotifyName());
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

}
