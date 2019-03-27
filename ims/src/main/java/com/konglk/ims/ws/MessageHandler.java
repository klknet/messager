package com.konglk.ims.ws;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.domain.UserDO;
import com.konglk.ims.service.UserService;
import com.konglk.model.Login;
import com.konglk.model.Request;
import com.konglk.model.Response;
import java.io.IOException;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageHandler
{
  @Autowired
  private UserService userService;
  
  public void process(Request request, ChatEndPoint client)
    throws IOException
  {
    switch (request.getType())
    {
    case 0: 
      client.getSession().getBasicRemote().sendText(JSON.toJSONString(new Response(200, null, "pong", 0)));
      break;
    case 1: 
      Login login = (Login)JSON.parseObject(request.getData(), Login.class);
      UserDO userDO = this.userService.login(login.getUnique(), login.getPwd());
      if (userDO == null)
      {
        client.getSession().getBasicRemote().sendText(JSON.toJSONString(new Response(500, "", null, 1)));
      }
      else
      {
        client.setUserId(userDO.getUserId());
        client.getSession().getBasicRemote()
          .sendText(JSON.toJSONString(new Response(200, "", JSON.toJSONString(userDO), 1)));
      }
      break;
    }
  }
}
