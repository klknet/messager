package com.konglk.ims.event;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.service.TopicNameManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by konglk on 2019/4/20.
 */
@Service
public class TopicProducer {
    @Autowired
    private TopicNameManager topicNameManager;
    @Resource(name = "jmsTopicTemplate")
    private JmsTemplate jmsTemplate;

    /**
     * 发送聊天topic
     * @param text
     * @param route
     */
    public void sendChatMessage(String text, int hash) {
        jmsTemplate.convertAndSend(topicNameManager.getChatName(hash), text);
    }

    /**
     * 通知类topic
     */
    public void sendNotifyMessage(ResponseEvent event) {
        jmsTemplate.convertAndSend(topicNameManager.getNotifyName(), JSON.toJSONString(event));
    }
}
