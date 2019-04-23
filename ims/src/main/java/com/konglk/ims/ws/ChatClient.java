package com.konglk.ims.ws;

import com.konglk.ims.cache.RedisCacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatClient {
    private static final Map<String, ChatEndPoint> clientMap = new ConcurrentHashMap(100);

    @Autowired
    private RedisCacheService cacheService;

    public void addClient(String key, ChatEndPoint chatEndPoint) {
        clientMap.put(key, chatEndPoint);
    }

    public ChatEndPoint getClient(String key) {
        return clientMap.get(key);
    }

    public void removeClient(String key) {
        if(StringUtils.isNotEmpty(key))
            clientMap.remove(key);
    }

    public void addTicket(String key, String ticket) {
//        ticketMap.put(key, ticket);
        if(StringUtils.isNotEmpty(key))
            cacheService.setUserTicket(key, ticket);
    }

    public String getTicket(String key) {
        return cacheService.getUserTicket(key);
    }

    public void removeTicket(String key) {
    }
}
