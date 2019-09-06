package com.konglk.ims.ws;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.domain.GroupChatDO;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.event.TopicProducer;
import com.konglk.ims.service.ConversationService;
import com.konglk.ims.service.MessageService;
import com.konglk.ims.service.ReplyService;
import com.konglk.model.Request;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MessageHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TopicProducer producer;
    @Autowired
    private PresenceManager presenceManager;
    @Autowired
    private ReplyService replyService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private ThreadPoolTaskExecutor executor;
    @Autowired
    private RedisCacheService redisCacheService;

    public void process(Request request, ChatEndPoint client) throws IOException {
        switch (request.getType()) {
            case 0:  //ping
                replyService.replyPong(client);
                client.setTimestamp(System.currentTimeMillis());
                break;
            case 1:  //ack
                String msgId = request.getData();
                redisCacheService.ackMsg(msgId, client.getUserId());
                break;
            case 2:  //message
                MessageDO messageDO = JSON.parseObject(request.getData(), MessageDO.class);
                if (StringUtils.isEmpty(messageDO.getContent()))
                    return;
                if (messageService.existsByMsgId(messageDO.getMessageId())){
                    return;
                }
                messageService.insert(messageDO);
                conversationService.updateConversation(messageDO);
                final MessageDO m = messageDO;
                executor.submit(()->incrementUnread(m));
                //消息发送到mq
                producer.sendChatMessage(request.getData(), messageDO.getConversationId());
                client.getSession().getBasicRemote().sendText(JSON.toJSONString(new Response(ResponseStatus.M_ACK, Response.MESSAGE, messageDO.getMessageId())));
                break;
        }
    }

    protected void incrementUnread(MessageDO messageDO) {
        if(messageDO.getChatType() == 0) {
            redisCacheService.incUnreadNum(messageDO.getDestId(), messageDO.getConversationId(), 1);
        }else {
            GroupChatDO groupChat = conversationService.findGroupChat(messageDO.getDestId());
            if(groupChat != null && !CollectionUtils.isEmpty(groupChat.getMembers())) {
                List<String> userIds = groupChat.getMembers().stream()
                        .filter(member -> !member.getUserId().equals(messageDO.getUserId())) //过滤自己
                        .map(member -> member.getUserId()).collect(Collectors.toList());
                redisCacheService.incUnreadNum(userIds, messageDO.getConversationId(), 1);
            }
        }
    }
}
