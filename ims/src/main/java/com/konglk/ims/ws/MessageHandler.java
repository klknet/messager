package com.konglk.ims.ws;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.event.TopicProducer;
import com.konglk.model.Request;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class MessageHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TopicProducer producer;
    @Autowired
    private ThreadPoolTaskExecutor executor;
    @Autowired
    private RedisCacheService redisCacheService;
    private List<MessageDO> msgQueue = new ArrayList<>(64);

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
                //消息发送到mq
                producer.sendChatMessage(request.getData(), client.getConversationHash(messageDO.getConversationId()));
                client.send(new Response(ResponseStatus.M_ACK, Response.MESSAGE, messageDO.getMessageId()));
                long diff = System.currentTimeMillis()-messageDO.getCreateTime().getTime();
                if (diff > 500)
                    logger.info("slow send msg cost time {}", diff);
                break;
        }
    }



    public List<MessageDO> getMsgQueue() {
        return msgQueue;
    }
}
