package com.konglk.ims.event;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.util.SpringUtils;
import com.konglk.ims.ws.PresenceManager;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * 接收通知类消息
 * Created by konglk on 2019/8/15.
 */
@Service
public class NotifyListenerImpl implements MessageListener {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PresenceManager presenceManager;
    @Autowired
    private SpringUtils springUtils;

    @Override
    public void onMessage(Message message) {
        if (message instanceof ActiveMQTextMessage) {
            String text = "";
            ResponseEvent event = null;
            try {
                ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;
                text = textMessage.getText();
                event = JSON.parseObject(text, ResponseEvent.class);
                if (presenceManager.existsUser(event.getUserId())) {
                    springUtils.getApplicationContext().publishEvent(event);
                }
            } catch (JMSException jms) {
                logger.error(jms.getMessage(), jms);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }
}
