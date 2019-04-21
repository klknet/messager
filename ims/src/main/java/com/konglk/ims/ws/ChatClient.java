package com.konglk.ims.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatClient {
    private static final Map<String, ChatEndPoint> clientMap = new ConcurrentHashMap(100);
    private static final Map<String, String> ticketMap = new ConcurrentHashMap(100);

    public static void addClient(String key, ChatEndPoint chatEndPoint) {
        clientMap.put(key, chatEndPoint);
    }

    public static ChatEndPoint getClient(String key) {
        return (ChatEndPoint) clientMap.get(key);
    }

    public static void removeClient(String key) {
        clientMap.remove(key);
    }

    public static void addTicket(String key, String ticket) {
        ticketMap.put(key, ticket);
    }

    public static String getTicket(String key) {
        return ticketMap.get(key);
    }

    public static void removeTicket(String key) {
        ticketMap.remove(key);
    }
}
