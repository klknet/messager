package com.konglk.ims.ws;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.domain.UserDO;
import com.konglk.ims.util.SpringUtils;
import com.konglk.model.Request;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@ClientEndpoint
public class WebsocketClient {
    Session userSession;
    ThreadPoolTaskScheduler taskScheduler;
    int seq;
    UserDO user;


    public WebsocketClient(URI endpointURI, UserDO user) {
        try {
            this.user = user;
            taskScheduler = SpringUtils.getBeanObj(ThreadPoolTaskScheduler.class);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket "+user.getUsername());
        this.userSession = userSession;
        sendMessage(new Request(0, "ping"));
        taskScheduler.scheduleAtFixedRate(() -> {
            if (seq >= 8) {
                release();
                return;
            }
            MessageDO messageDO = getMessageDO();
            sendMessage(new Request(2, JSON.toJSONString(messageDO)));
        }, 8000L);
    }



    @OnMessage
    public void onMessage(String message) {
        try {
            Response response = JSON.parseObject(message, Response.class);
            if (response.getType() == 0) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, 5);
                taskScheduler.schedule(()->
                        sendMessage(new Request(0, "ping")), cal.getTime());
            }
            if (response.getType() == Response.MESSAGE && response.getCode() == ResponseStatus.M_TRANSFER_MESSAGE.getCode()) {
                MessageDO msg = JSON.parseObject(response.getData(), MessageDO.class);
                sendMessage(new Request(1, msg.getMessageId()));
                System.out.println(user.getUsername()+" receive from "+msg.getContent()+" "+msg.getCreateTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose() {
        System.out.println("closed "+user.getUsername());
    }

    @OnError
    public void onError(Throwable t) {
        System.out.println("error "+t.getMessage());
    }

    public void release() {
        if (userSession.isOpen()) {
            try {
                userSession.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(Request message) {
        try {
            if(userSession.isOpen()) {
                synchronized (userSession) {
                    userSession.getBasicRemote().sendText(JSON.toJSONString(message));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MessageDO getMessageDO() {
        MessageDO messageDO = new MessageDO();
        messageDO.setConversationId("e1cccd89-ca77-4b1f-b0d3-7c3a95c5e726");
        messageDO.setChatType(1);
        messageDO.setContent(user.getUsername()+"-"+seq++);
        messageDO.setType(0);
        messageDO.setUserId(user.getUserId());
        messageDO.setCreateTime(new Date());
        messageDO.setDestId("9e65aa92-b724-41c3-808e-0ea5d2c86eba");
        messageDO.setMessageId(UUID.randomUUID().toString());
        return messageDO;
    }
}
