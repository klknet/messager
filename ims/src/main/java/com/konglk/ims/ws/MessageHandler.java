package com.konglk.ims.ws;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.service.ChatProducer;
import com.konglk.ims.service.ReplyService;
import com.konglk.ims.service.UserService;
import com.konglk.model.Authentication;
import com.konglk.model.ResponseStatus;
import com.konglk.model.Request;
import com.konglk.model.Response;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageHandler {
    @Autowired
    private UserService userService;
    @Autowired
    private ChatProducer producer;
    @Autowired
    private ConnectionHolder connectionHolder;
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
                String ticket = connectionHolder.getTicket(authentication.getUserId());
                if (StringUtils.isEmpty(authentication.getUserId())) {
                    client.getSession().close();
                }
                if (StringUtils.isNotEmpty(request.getTicket()) &&
                        StringUtils.equals(ticket, request.getTicket())){
                    client.setUserId(authentication.getUserId());
                    client.setTicket(ticket);
                    client.setAuth(true);
                    connectionHolder.addClient(authentication.getUserId(), client);
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
                    //保证消息的幂等性
                    messageDO.setMessageId(UUID.randomUUID().toString());
                    //消息发送到mq
                    producer.send(JSON.toJSONString(messageDO));
                    //消息回复给发送者
                    Response resp = new Response(200, "copy", request.getData(), Response.MESSAGE);
                    replyService.reply(client, resp);
                }else {
                    client.getSession().close();
                }
                break;
        }
    }
}
