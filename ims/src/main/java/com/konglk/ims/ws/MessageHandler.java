package com.konglk.ims.ws;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.domain.GroupChatDO;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.event.TopicProducer;
import com.konglk.ims.repo.IMessageRepository;
import com.konglk.ims.service.ConversationService;
import com.konglk.ims.service.MessageService;
import com.konglk.model.Request;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Component
public class MessageHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TopicProducer producer;
    @Autowired
    private MessageService messageService;
    @Autowired
    private IMessageRepository messageRepository;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private RedisCacheService redisCacheService;
    private Deque<MessageDO> msgQueue = new ConcurrentLinkedDeque<>();

    public void process(Request request, ChatEndPoint client) throws IOException {
        switch (request.getType()) {
            case 0:  //ping
                client.setTimestamp(System.currentTimeMillis());
                client.send(new Response(200, null, "pong", Response.HEART));
                break;
            case 1:  //ack
                String msgId = request.getData();
                redisCacheService.ackMsg(msgId, client.getUserId());
                break;
            case 2:  //message
                MessageDO messageDO = JSON.parseObject(request.getData(), MessageDO.class);
                if (StringUtils.isEmpty(messageDO.getContent()))
                    return;
                if(messageDO.getCreateTime() == null) {
                    messageDO.setCreateTime(new Date());
                }
                //异步执行入库操作，增加消息的响应速度， 可批量处理
                msgQueue.add(messageDO);
                client.send(new Response(ResponseStatus.M_ACK, Response.MESSAGE, messageDO.getMessageId()));
                //消息发送到mq
                producer.sendChatMessage(request.getData(), client.getConversationHash(messageDO.getConversationId()));
                long diff = System.currentTimeMillis()-messageDO.getCreateTime().getTime();
                if (diff > 500)
                    logger.info("slow send msg cost time {}", diff);
                break;
        }
    }

    @Scheduled(cron = "*/32 * * * * *")
    public void persistMsg() {
        if (msgQueue.size() > 0) {
            Deque<MessageDO> msgDOs = new LinkedList<>(msgQueue);
            msgQueue.removeAll(msgDOs);
            messageService.insertAll(msgDOs);
            incrementUnread(msgDOs);
            //更新最后一条消息的内容、时间
            Map<String, MessageDO> convMap = msgDOs.stream().collect(Collectors.groupingBy(MessageDO::getConversationId,
                    Collectors.collectingAndThen(Collectors.toList(), v -> v.get(v.size()-1))));
            for (String cid: convMap.keySet()) {
                conversationService.updateConversation(convMap.get(cid));
            }
        }
    }

    protected void incrementUnread(Deque<MessageDO> messageDOs) {
        Map<String, Map<String, Long>> singleInc = new HashMap<>();
        Map<String, MutableTriple<String, String, Long>> groupInc = new HashMap<>();
        for (MessageDO messageDO: messageDOs) {
            if (messageDO.getChatType() == 0) {
                if (singleInc.containsKey(messageDO.getDestId())) {
                    Map<String, Long> map = singleInc.get(messageDO.getDestId());
                    map.put(messageDO.getConversationId(), map.get(messageDO.getConversationId()) + 1L);
                } else {
                    Map<String, Long> map = new HashMap<>();
                    map.put(messageDO.getConversationId(), 1L);
                    singleInc.put(messageDO.getDestId(), map);
                }
            } else {
                if (groupInc.containsKey(messageDO.getDestId())) {
                    MutableTriple<String, String, Long> triple = groupInc.get(messageDO.getDestId());
                    triple.setRight(triple.getRight() + 1L);
                }else {
                    MutableTriple<String, String, Long> triple = new MutableTriple<>(messageDO.getConversationId(), messageDO.getUserId(), 1L);
                    groupInc.put(messageDO.getDestId(), triple);
                }
            }
        }
        for (String destId: singleInc.keySet()) {
            for (String conversationId: singleInc.get(destId).keySet()) {
                redisCacheService.incUnreadNum(destId, conversationId, singleInc.get(destId).get(conversationId));
            }
        }

        for (String destId: groupInc.keySet()) {
            MutableTriple<String, String, Long> triple = groupInc.get(destId);
            List<GroupChatDO> groupChat = conversationService.findGroupChat(destId);
            if(groupChat != null && !CollectionUtils.isEmpty(groupChat)) {
                List<String> userIds = groupChat.stream()
                        .filter(member -> !member.getUserId().equals(triple.getMiddle())) //过滤自己
                        .map(member -> member.getUserId()).collect(Collectors.toList());
                redisCacheService.incUnreadNum(userIds, triple.getLeft(), triple.getRight());
            }
        }
    }

}
