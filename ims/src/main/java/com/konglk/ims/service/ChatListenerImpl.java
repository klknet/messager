package com.konglk.ims.service;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.domain.FailedMessageDO;
import com.konglk.ims.domain.GroupChatDO;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.event.ResponseEvent;
import com.konglk.ims.util.SpringUtils;
import com.konglk.ims.ws.ConnectionHolder;
import com.konglk.model.Response;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by konglk on 2019/4/20.
 */
@Service
public class ChatListenerImpl implements MessageListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MessageService messageService;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private SpringUtils springUtils;
    @Autowired
    private ThreadPoolTaskExecutor executor;
    @Autowired
    private RedisCacheService redisCacheService;

    @Override
    public void onMessage(Message message) {
        if(message instanceof ActiveMQTextMessage) {
            ActiveMQTextMessage textMessage = (ActiveMQTextMessage) message;
            MessageDO messageDO = null;
            String text = null;
            try {
                text = textMessage.getText();
                messageDO = JSON.parseObject(text, MessageDO.class);
                messageService.insert(messageDO);
                conversationService.updateConversation(messageDO);

                final MessageDO m = messageDO;
                final String t = text;
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        incrementUnread(m);
                    }
                });
                //消息处理事件
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        publishMessageEvent(m, t);
                    }
                });
            }catch(JMSException jms) {
                logger.error(jms.getMessage(), jms);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                if (messageDO != null) {
                    FailedMessageDO msg = new FailedMessageDO();
                    msg.setMessageId(messageDO.getMessageId());
                    msg.setText(text);
                    msg.setTs(new Date());
                    messageService.failedMessge(msg);
                }
            }
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

    public void publishMessageEvent(MessageDO messageDO, String text) {
        if(messageDO.getChatType() == 0) {
            // 一对一单聊消息
            ResponseEvent event = new ResponseEvent(new Response(200, "", text, Response.MESSAGE), messageDO.getUserId());
            springUtils.getApplicationContext().publishEvent(event);
            event = new ResponseEvent(new Response(200, "", text, Response.MESSAGE), messageDO.getDestId());
            springUtils.getApplicationContext().publishEvent(event);
        } else {
            //群聊消息
            GroupChatDO groupChat = conversationService.findGroupChat(messageDO.getDestId());
            if(groupChat != null && !CollectionUtils.isEmpty(groupChat.getMembers())) {
                for (GroupChatDO.Member member: groupChat.getMembers()) {
                    ResponseEvent event = new ResponseEvent(new Response(200, "", text, Response.MESSAGE), member.getUserId());
                    springUtils.getApplicationContext().publishEvent(event);
                }
            }
        }
    }

}
