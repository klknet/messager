package com.konglk.ims.ws;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.event.TopicProducer;
import com.konglk.ims.service.ReplyService;
import com.konglk.model.Authentication;
import com.konglk.model.Request;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MessageHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TopicProducer producer;
    @Autowired
    private PresenceManager presenceManager;
    @Autowired
    private ReplyService replyService;

    public void process(Request request, ChatEndPoint client)
            throws IOException {
        switch (request.getType()) {
            case 0:
                replyService.replyPong(client);
                break;
            case 2:
                MessageDO messageDO = JSON.parseObject(request.getData(), MessageDO.class);
                if (StringUtils.isEmpty(messageDO.getContent()))
                    return;
                if (client.isAuth()){
                    //消息发送到mq
                    producer.sendChatMessage(request.getData(), messageDO.getConversationId());
                }else {
                    logger.warn("user {} not authenticated", messageDO.getUserId());
                    client.getSession().close();
                }
                break;
        }
    }
}
