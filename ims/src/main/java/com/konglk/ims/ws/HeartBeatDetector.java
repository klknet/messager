package com.konglk.ims.ws;

import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.domain.GroupChatDO;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.repo.IMessageRepository;
import com.konglk.ims.service.ConversationService;
import com.konglk.ims.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by konglk on 2019/8/28.
 */
@Service
public class HeartBeatDetector {
    @Autowired
    private PresenceManager presenceManager;
    @Autowired
    private MessageHandler messageHandler;
    @Autowired
    private MessageService messageService;
    @Autowired
    private IMessageRepository messageRepository;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private RedisCacheService redisCacheService;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final long liveTime = 60000L;

    @Scheduled(cron = "0 */1 * * * *")
    public void detect() {
        Map<String, ChatEndPoint> clientMap = presenceManager.getClientMap();
        if (clientMap != null && clientMap.size()>0) {
            Iterator<Map.Entry<String, ChatEndPoint>> iterator = clientMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ChatEndPoint> entry = iterator.next();
                long cur = System.currentTimeMillis();
                if (Math.abs(cur - entry.getValue().getTimestamp()) > liveTime) {
                    ChatEndPoint client = entry.getValue();
                    logger.info("remove timeout connection {}", client.getNickname());
                    //移除在线状态，关闭client连接
                    iterator.remove();
                    client.release();
                }
            }
        }
    }

    @Scheduled(cron = "0 */2 * * * *")
    public void insertMsg() {
        List<MessageDO> msgQueue = new ArrayList<>(messageHandler.getMsgQueue());
        if (msgQueue.size() > 0) {
            messageRepository.saveAll(msgQueue);
            conversationService.updateConversation(msgQueue.get(msgQueue.size()-1));
            incrementUnread(msgQueue.get(msgQueue.size()-1));
            messageHandler.getMsgQueue().removeAll(msgQueue);
        }

    }

    protected void incrementUnread(MessageDO messageDO) {
        if(messageDO.getChatType() == 0) {
            redisCacheService.incUnreadNum(messageDO.getDestId(), messageDO.getConversationId(), 1);
        }else {
            List<GroupChatDO> groupChat = conversationService.findGroupChat(messageDO.getDestId());
            if(groupChat != null && !CollectionUtils.isEmpty(groupChat)) {
                List<String> userIds = groupChat.stream()
                        .filter(member -> !member.getUserId().equals(messageDO.getUserId())) //过滤自己
                        .map(member -> member.getUserId()).collect(Collectors.toList());
                redisCacheService.incUnreadNum(userIds, messageDO.getConversationId(), 1);
            }
        }
    }
}
