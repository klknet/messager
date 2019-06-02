package com.konglk.ims.service;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.ws.ChatEndPoint;
import com.konglk.model.ResponseStatus;
import com.konglk.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by konglk on 2019/6/2.
 */
@Service
public class ReplyService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void reply(ChatEndPoint client, Response response) {
        if(client == null)
            return;
        try {
            client.getSession().getBasicRemote()
                    .sendText(JSON.toJSONString(response));
            if (response.getType() == Response.AUTH)
                logger.info("{}-{} reply message - {}", client.getNickname(), client.getUserId(), response.getMessage());
        } catch (IOException e) {
            logger.error("reply error", e.getMessage());
        }
    }

    public void replyOK(ChatEndPoint client) {
        reply(client, new Response(200, "authentication success", "", Response.AUTH));
    }

    public void replyTicketError(ChatEndPoint client) {
        reply(client, new Response(ResponseStatus.TICKET_ERROR, Response.AUTH));
    }

    public void replyPong(ChatEndPoint client) {
        reply(client, new Response(200, null, "pong", Response.HEART));
    }

    public void replyKickout(ChatEndPoint client) {
        reply(client,new Response(ResponseStatus.KICK_OUT, 1));
    }

}
