package com.konglk.ims.event;

import com.konglk.ims.service.TopicNameManager;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.jms.*;

/**
 * Created by konglk on 2019/4/20.
 */
@Service
//@Profile("local")
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
        TopicConnection connection = factory.createTopicConnection();
        for(int i=0; i<names.length; i++) {
            //聊天消息topic
            connection.start();
            TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(names[i]);
            MessageConsumer consumer = session.createConsumer(topic);
            consumer.setMessageListener(chatListener);
            logger.info("consumer ready for {}", names[i]);
        }
        //通知类topic
        connection = factory.createTopicConnection();
        connection.start();
        TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicNameManager.getNotifyName());
        MessageConsumer consumer = session.createConsumer(topic);
        consumer.setMessageListener(notifyListener);
        logger.info("consumer ready for {}", topicNameManager.getNotifyName());
    }


}
