package com.konglk.ims.ws;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.util.RegExpUtil;
import com.konglk.ims.util.SpringUtils;
import com.konglk.model.Request;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ServerEndpoint(value = "/ws/chat")
public class ChatEndPoint {
    private static final AtomicLong connectionIds = new AtomicLong();
    private static final Logger logger = LoggerFactory.getLogger(ChatEndPoint.class);

    private String nickname;
    private String userId;
    private String ticket;
    private boolean auth;
    private long timestamp;

    private Session session;
    private MessageHandler messageHandler;
    private PresenceManager presenceManager;

    public ChatEndPoint() {
        this.nickname = ("client:" + connectionIds.getAndIncrement());
        this.messageHandler = SpringUtils.getBeanObj(MessageHandler.class);
        this.presenceManager = SpringUtils.getBeanObj(PresenceManager.class);
        this.timestamp = System.currentTimeMillis();
    }

    @OnOpen
    public void connect(Session session) {
        String queryString = session.getQueryString();
        boolean auth = false;
        String userId = null, ticket = null;
        if (StringUtils.isNotEmpty(queryString)) {
            userId = RegExpUtil.getUrlParameter(queryString, "userId");
            ticket = RegExpUtil.getUrlParameter(queryString, "ticket");
            if (StringUtils.isNotEmpty(userId) || StringUtils.isNotEmpty(ticket) ||
                    ticket.equals(presenceManager.getTicket(userId))) {
                auth = true;
            }
        }
        if (!auth) {
            try {
                session.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return;
        }
        logger.info("new connection active {}, {}, {}", this.nickname, userId, ticket);
        this.session = session;
        this.userId = userId;
        this.ticket = ticket;
        this.auth = true;
        presenceManager.addClient(userId, this);
    }

    @OnMessage
    public void incoming(String message)
            throws Exception {
        logger.debug(message);
        Request request = (Request) JSON.parseObject(message, Request.class);
        this.messageHandler.process(request, this);
    }

    @OnMessage
    public void incoming(ByteBuffer buffer) throws IOException {
        logger.info("receive file");
        RandomAccessFile out = new RandomAccessFile(new File("d:/tt"), "rw");
        out.seek(out.length());
        out.write(buffer.array());
        out.close();
    }

    @OnClose
    public void close() {
        presenceManager.removeClient(this.userId);
        logger.info("client {} leaves", this.nickname);
    }

    @OnError
    public void onError(Throwable t)
            throws Throwable {
        logger.error("WebSocket error." + t.getMessage(), t);
        this.session.close();
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
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
}
