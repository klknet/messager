package com.konglk.ims.ws;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.util.RegExpUtil;
import com.konglk.ims.util.SpringUtils;
import com.konglk.model.Request;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@ServerEndpoint(value = "/ws/chat")
public class ChatEndPoint {
    private static final AtomicLong connectionIds = new AtomicLong();
    private static final Logger logger = LoggerFactory.getLogger(ChatEndPoint.class);

    private String nickname;
    private String userId;
    private boolean auth;
    private boolean dropDown;//是否挤出标志
    private long timestamp;
    private Map<String, Integer> hash;

    private Session session;
    private MessageHandler messageHandler;
    private PresenceManager presenceManager;

    public ChatEndPoint() {
        this.nickname = ("client:" + connectionIds.getAndIncrement());
        this.messageHandler = SpringUtils.getBeanObj(MessageHandler.class);
        this.presenceManager = SpringUtils.getBeanObj(PresenceManager.class);
        this.timestamp = System.currentTimeMillis();
        this.hash = new HashMap<>();
    }

    @OnOpen
    public void connect(Session session) {
        String queryString = session.getQueryString();
        boolean auth = false;
        String userId = null, ticket = null;
        if (StringUtils.isNotEmpty(queryString)) {
            userId = RegExpUtil.getUrlParameter(queryString, "userId");
            ticket = RegExpUtil.getUrlParameter(queryString, "ticket");
            if (StringUtils.isNotEmpty(userId) && StringUtils.isNotEmpty(ticket) &&
                    ticket.equals(presenceManager.getTicket(userId))) {
                auth = true;
            }
        }
        this.session = session;
        this.userId = userId;
        if (!auth) {
            send(new Response(ResponseStatus.TICKET_ERROR, Response.USER));
            this.release();
            return;
        }
        logger.info("new connection active {} - {}", this.nickname, this.userId);
        this.auth = true;
        if (presenceManager.existsUser(userId)) {
            ChatEndPoint endPoint = presenceManager.getClient(userId);
            endPoint.setDropDown(true);
            endPoint.auth = false;
            endPoint.release();
        }
        presenceManager.addClient(userId, this);
    }

    @OnMessage
    public void incoming(String message) {
        if (!auth) {
            return;
        }
        logger.debug(message);
        try {
            Request request = JSON.parseObject(message, Request.class);
            this.messageHandler.process(request, this);
        } catch (Throwable e) {
            logger.error("processing message error. "+e.getMessage(), e);
        }
    }

    @OnClose
    public void onClose() {
        //对于被挤下去的用户，不要剔除在线模块
        if (!dropDown) {
            presenceManager.removeClient(this.userId);
        }
        logger.info("client {}-{} leaves", this.nickname, this.userId);
    }

    @OnError
    public void onError(Throwable t) {
        logger.error("WebSocket error. "+this.nickname, t);
    }

    public void send(Response response) {
        try {
            String text = JSON.toJSONString(response);
            if (session.isOpen()) {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.getBasicRemote()
                                .sendText(text);
                        if (response.getType() == Response.USER)
                            logger.info("{}-{} reply message - {}", nickname, userId, response.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("reply error", e.getMessage());
        }
    }

    public int getConversationHash(String conversationId) {
        if (hash.containsKey(conversationId))
            return hash.get(conversationId);
        int hashCode = conversationId.hashCode();
        hash.put(conversationId, hashCode & Integer.MAX_VALUE);
        return hashCode;
    }

    public void release() {
        if (this.session.isOpen()) {
            try {
                this.session.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAuth() {
        return auth;
    }

    public String getNickname() {
        return this.nickname;
    }

    public Session getSession() {
        return this.session;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void setPresenceManager(PresenceManager presenceManager) {
        this.presenceManager = presenceManager;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDropDown() {
        return dropDown;
    }

    public void setDropDown(boolean dropDown) {
        this.dropDown = dropDown;
    }
}
