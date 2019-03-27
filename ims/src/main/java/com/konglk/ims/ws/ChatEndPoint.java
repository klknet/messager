package com.konglk.ims.ws;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.util.SpringUtils;
import com.konglk.model.Request;
import java.util.concurrent.atomic.AtomicInteger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/ws/chat")
public class ChatEndPoint
{
  private static final AtomicInteger connectionIds = new AtomicInteger();
  private static final Logger logger = LoggerFactory.getLogger(ChatEndPoint.class);
  private String nickname;
  private String userId;
  private Session session;
  private MessageHandler messageHandler;
  
  public ChatEndPoint()
  {
    this.nickname = ("client:" + connectionIds.getAndIncrement());
    messageHandler = SpringUtils.getBean(MessageHandler.class);
  }
  
  @OnOpen
  public void connect(Session session)
  {
    logger.debug("new connection active {}", this.nickname);
    this.session = session;
    ChatClient.addClient(this.nickname, this);
  }
  
  @OnMessage
  public void incoming(String message)
    throws Exception
  {
    logger.debug(message);
    Request request = (Request)JSON.parseObject(message, Request.class);
    this.messageHandler.process(request, this);
  }
  
  @OnClose
  public void close()
  {
    ChatClient.removeClient(this.nickname);
    logger.debug("client {} leaves", this.nickname);
  }
  
  @OnError
  public void onError(Throwable t)
    throws Throwable
  {
    logger.error("WebSocket ����" + t);
  }
  
  public String getUserId()
  {
    return this.userId;
  }
  
  public void setUserId(String userId)
  {
    this.userId = userId;
  }
  
  public String getNickname()
  {
    return this.nickname;
  }
  
  public Session getSession()
  {
    return this.session;
  }
}
