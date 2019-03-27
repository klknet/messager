package com.konglk.ims.ws;

import java.util.Set;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;

public class WebsocketConfig
  implements ServerApplicationConfig
{
  public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> scanned)
  {
    return null;
  }
  
  public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned)
  {
    return scanned;
  }
}
