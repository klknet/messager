package com.konglk.ims.service;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.domain.FailedMessageDO;
import com.konglk.ims.domain.GroupChatDO;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.event.ResponseEvent;
import com.konglk.ims.util.SpringUtils;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by konglk on 2019/4/20.
 */
@Service
public class MessageService {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private RedisCacheService cacheService;
    @Autowired
    private SpringUtils springUtils;

    public List<MessageDO> prevMessages(String cid, Date createtime, boolean include) {
        Query query = new Query();
        //是否包含当前时间点的消息
        if (include) {
            query.addCriteria(Criteria.where("conversationId").is(cid).and("createTime").gte(createtime));
        } else {
            query.addCriteria(Criteria.where("conversationId").is(cid).and("createTime").gt(createtime));
        }
        query.addCriteria(Criteria.where("type").gte(0));
        query.with(PageRequest.of(0, 32, Sort.by(Sort.Direction.DESC, "createTime")));
        List<MessageDO> messageDOS = mongoTemplate.find(query, MessageDO.class);
        Collections.reverse(messageDOS);
        return messageDOS;
    }

    public void insert(MessageDO messageDO) {
        if(messageDO.getCreateTime() == null) {
            messageDO.setCreateTime(new Date());
        }
        mongoTemplate.insert(messageDO);
    }

    public void failedMessge(FailedMessageDO msg) {
        mongoTemplate.insert(msg);
    }

    /*
    撤回消息
     */
    public void revocation(String userId, String msgId) {
        updateMsgType(userId, msgId, 5);
        notifyRevocation(msgId);
    }

    /*
    更新消息类型
     */
    public void updateMsgType(String userId, String msgId, int type) {
        Query query = new Query(Criteria.where("message_id").is(msgId).and("user_id").is(userId));
        Update update = Update.update("type", type);
        mongoTemplate.updateFirst(query, update, MessageDO.class);
    }

    public MessageDO findByMsgId(String msgId) {
        return mongoTemplate.findOne(Query.query(Criteria.where("message_id").is(msgId)), MessageDO.class);
    }

    /*
    通知对方撤回消息
     */
    public void notifyRevocation(String msgId) {
        MessageDO message = findByMsgId(msgId);
        notify(message, new Response(ResponseStatus.REVOCATION, Response.MESSAGE, JSON.toJSONString(message)));
    }

    /*
    消息变动时通知地方
     */
    public void notify(MessageDO message, Response response) {
        if (message != null) {
            if(message.getChatType() == 0) {
                ResponseEvent event = new ResponseEvent(response, message.getDestId());
                springUtils.getApplicationContext().publishEvent(event);
            }else if (message.getChatType() == 1) {
                GroupChatDO groupChat = conversationService.findGroupChat(message.getDestId());
                groupChat.getMembers().forEach(m -> {
                    ResponseEvent event = new ResponseEvent(response, m.getUserId());
                    springUtils.getApplicationContext().publishEvent(event);
                });
            }
        }
    }


}
