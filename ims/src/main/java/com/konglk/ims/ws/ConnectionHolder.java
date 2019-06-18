package com.konglk.ims.ws;

import com.konglk.ims.cache.RedisCacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectionHolder {
    private static final Map<String, ChatEndPoint> clientMap = new ConcurrentHashMap(100);

    @Autowired
    private RedisCacheService cacheService;

    public void addClient(String userId, ChatEndPoint chatEndPoint) {
        clientMap.put(userId, chatEndPoint);
    }

    public ChatEndPoint getClient(String userId) {
        return clientMap.get(userId);
    }

    public void removeClient(String userId) {
        if(StringUtils.isNotEmpty(userId))
            clientMap.remove(userId);
    }

    public void addTicket(String userId, String ticket) {
//        ticketMap.put(key, ticket);
        if(StringUtils.isNotEmpty(userId))
            cacheService.setUserTicket(userId, ticket);
    }

    public String getTicket(String userId) {
        return cacheService.getUserTicket(userId);
    }

    public void removeTicket(String userId) {
        cacheService.delUserTicket(userId);
    }
}
