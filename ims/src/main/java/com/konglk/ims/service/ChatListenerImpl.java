package com.konglk.ims.service;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.domain.FailedMessageDO;
import com.konglk.ims.domain.GroupChatDO;
import com.konglk.ims.domain.MessageDO;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
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
                //未读消息+1
                executor.submit(()->incrementUnread(m));
                //消息处理事件
                executor.submit(()->messageService.notify(m, new Response(ResponseStatus.TRANSFER_MESSAGE, Response.MESSAGE)));
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


}
