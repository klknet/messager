package com.konglk.ims.ws;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.service.ChatProducer;
import com.konglk.ims.service.ReplyService;
import com.konglk.ims.service.UserService;
import com.konglk.model.Authentication;
import com.konglk.model.Request;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;
    @Autowired
    private ChatProducer producer;
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
            case 1:
                Authentication authentication = JSON.parseObject(request.getData(), Authentication.class);
                String ticket = presenceManager.getTicket(authentication.getUserId());
                if (StringUtils.isEmpty(authentication.getUserId())) {
                    client.getSession().close();
                }
                if (StringUtils.isNotEmpty(request.getTicket()) &&
                        StringUtils.equals(ticket, request.getTicket())){
                    client.setUserId(authentication.getUserId());
                    client.setTicket(ticket);
                    client.setAuth(true);
                    presenceManager.addClient(authentication.getUserId(), client);
                    replyService.replyOK(client);
                }else {
                    replyService.replyTicketError(client);
                }
                break;
            case 2:
                MessageDO messageDO = JSON.parseObject(request.getData(), MessageDO.class);
                if (StringUtils.isEmpty(messageDO.getContent()))
                    return;
                if (StringUtils.isNotEmpty(request.getTicket()) && client.isAuth() &&
                        StringUtils.equals(client.getTicket(), request.getTicket())){
                    //消息发送到mq
                    producer.send(request.getData(), messageDO.getConversationId());
                }else {
                    logger.warn("user {} not authenticated", messageDO.getUserId());
                    client.getSession().close();
                }
                break;
        }
    }
}
