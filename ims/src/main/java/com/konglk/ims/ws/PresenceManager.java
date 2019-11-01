package com.konglk.ims.ws;

import com.konglk.ims.cache.RedisCacheService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PresenceManager {
    private final Map<String, ChatEndPoint> clientMap = new ConcurrentHashMap<>(256);//在线客户端连接
    private Map<String, Integer> consumeMap = new ConcurrentHashMap<>(256);  //消息消费标识


    @Autowired
    private RedisCacheService cacheService;

    public void addClient(String userId, ChatEndPoint chatEndPoint) {
        clientMap.put(userId, chatEndPoint);
        cacheService.setOnline(userId);
    }

    public ChatEndPoint getClient(String userId) {
        return clientMap.get(userId);
    }

    public void removeClient(String userId) {
        if(StringUtils.isNotEmpty(userId) && existsUser(userId)) {
            clientMap.remove(userId);
            cacheService.setOffline(userId);
        }
    }

    public boolean existsUser(String userId) {
        return clientMap.containsKey(userId);
    }

    public void addTicket(String userId, String ticket) {
        if(StringUtils.isNotEmpty(userId))
            cacheService.setUserTicket(userId, ticket);
    }

    public String getTicket(String userId) {
        return cacheService.getUserTicket(userId);
    }

    public void removeTicket(String userId) {
        cacheService.delUserTicket(userId);
    }

    public boolean isOnline(String userId) {
        return cacheService.isOnline(userId);
    }

    public Map<String, ChatEndPoint> getClientMap() {
        return clientMap;
    }

    public void consumeMsg(String msgId) {
        consumeMap.put(msgId, 1);
    }

    public boolean haveMsg(String msgId) {
        return consumeMap.containsKey(msgId);
    }

    public void removeMsg(String msgId) {
        consumeMap.remove(msgId);
    }

}
