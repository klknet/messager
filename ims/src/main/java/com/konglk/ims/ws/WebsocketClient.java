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
import java.util.concurrent.ScheduledFuture;

@ClientEndpoint
public class WebsocketClient {
    Session userSession;
    ThreadPoolTaskScheduler taskScheduler;
    int seq;
    UserDO user;
    int num;
    ScheduledFuture future;


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
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, 10);
        taskScheduler.schedule(() -> {
            this.future = taskScheduler.scheduleAtFixedRate(() -> {
                if (seq >= 8) {
                    this.future.cancel(true);
                    return;
                }
                MessageDO messageDO = getMessageDO();
                sendMessage(new Request(2, JSON.toJSONString(messageDO)));
            }, 15000L);
        }, c.getTime());
    }



    @OnMessage
    public void onMessage(String message) {
        try {
            Response response = JSON.parseObject(message, Response.class);
            if (response.getType() == 0) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, 35);
                taskScheduler.schedule(()->
                        sendMessage(new Request(0, "ping")), cal.getTime());
            }
            if (response.getType() == Response.MESSAGE && response.getCode() == ResponseStatus.M_TRANSFER_MESSAGE.getCode()) {
                num++;
                MessageDO msg = JSON.parseObject(response.getData(), MessageDO.class);
                sendMessage(new Request(1, msg.getMessageId()));
                System.out.println(user.getUsername()+" receive from "+msg.getContent()+" "+msg.getCreateTime()+" "+num);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose() {
//        System.out.println("closed "+user.getUsername());
    }

    @OnError
    public void onError(Throwable t) {
        System.out.println("error "+t.getMessage());
    }

    public void release() {
        if (userSession.isOpen()) {
            try {
                System.out.println(user.getUsername()+" consumes "+num+" messages");
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
        messageDO.setConversationId("0a1d76eb-a4b5-4fbe-8a72-7720406dab1a");
        messageDO.setChatType(1);
        messageDO.setContent(user.getUsername()+"-"+seq++);
        messageDO.setType(0);
        messageDO.setUserId(user.getUserId());
        messageDO.setCreateTime(new Date());
        messageDO.setDestId("03f86884-d245-4a98-ad30-1e2787bd6b25");
        messageDO.setMessageId(UUID.randomUUID().toString());
        return messageDO;
    }
}
