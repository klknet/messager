package com.konglk.ims.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatClient
{
  private static final Map<String, ChatEndPoint> clientMap = new ConcurrentHashMap(100);
  
  public static void addClient(String key, ChatEndPoint chatEndPoint)
  {
    clientMap.put(key, chatEndPoint);
  }
  
  public static ChatEndPoint getClient(String key)
  {
    return (ChatEndPoint)clientMap.get(key);
  }
  
  public static void removeClient(String key)
  {
    clientMap.remove(key);
  }
}
