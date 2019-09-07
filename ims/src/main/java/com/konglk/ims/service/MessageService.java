package com.konglk.ims.service;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.domain.FailedMessageDO;
import com.konglk.ims.domain.GroupChatDO;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.event.ResponseEvent;
import com.konglk.ims.event.TopicProducer;
import com.konglk.ims.model.FileDetail;
import com.konglk.ims.repo.IMessageRepository;
import com.konglk.ims.util.SpringUtils;
import com.konglk.ims.ws.PresenceManager;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private SpringUtils springUtils;
    @Autowired
    private IMessageRepository messageRepository;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private PresenceManager presenceManager;
    @Autowired
    private TopicProducer topicProducer;

    public List<MessageDO> prevMessages(String cid, String userId, Date createtime, boolean include) {
        Query query = new Query();
        //是否包含当前时间点的消息
        if (include) {
            query.addCriteria(Criteria.where("createTime").gte(createtime));
        } else {
            query.addCriteria(Criteria.where("createTime").gt(createtime));
        }
        query.addCriteria(Criteria.where("conversationId").is(cid).and("type").gte(0).and("deleteIds").ne(userId));
        query.with(PageRequest.of(0, 32, Sort.by(Sort.Direction.DESC, "createTime")));
        List<MessageDO> messageDOS = mongoTemplate.find(query, MessageDO.class);
        messageDOS.forEach(m -> {
            if (new Integer(2).equals(m.getType())) {
                GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(m.getContent())));
                m.setFileDetail(new FileDetail(gridFSFile.getLength(), gridFSFile.getFilename(), gridFSFile.getMetadata().getString("_contentType")));
            }
        });
        Collections.reverse(messageDOS);
        return messageDOS;
    }

    public void insert(MessageDO messageDO) {
        if(messageDO.getCreateTime() == null) {
            messageDO.setCreateTime(new Date());
        }
        mongoTemplate.insert(messageDO);
    }

    public void failedMessage(FailedMessageDO msg) {
        mongoTemplate.insert(msg);
    }

    /*
    撤回消息
     */
    public void revocation(String userId, String msgId) {
        MessageDO message = findByMsgId(msgId);
        if (msgId == null)
            throw new IllegalArgumentException();
        long time = message.getCreateTime().getTime();
        long now = System.currentTimeMillis();
        //超过2分钟的不让撤回
        if (now-time > 2*60*1000L)
            return;
        //更新消息会话类型
        updateMsgType(userId, msgId, 5);
        conversationService.updateLastTime(message.getConversationId(), null, null, 5, null);
        //通知客户端消息变动
        Response response = new Response(ResponseStatus.M_REVOCATION, Response.MESSAGE, JSON.toJSONString(message));
        notifyAll(message, response);
    }


    /*
    更新消息类型
     */
    public void updateMsgType(String userId, String msgId, int type) {
        Query query = new Query(Criteria.where("messageId").is(msgId).and("user_id").is(userId));
        Update update = Update.update("type", type);
        mongoTemplate.updateFirst(query, update, MessageDO.class);
    }

    /**
     * 该条消息对指定用户删除
     * @param msgId
     * @param userId
     */
    public void addToDeleteList(String msgId, String userId) {
        Query query = new Query(Criteria.where("messageId").is(msgId));
        Update update = new Update();
        update.addToSet("deleteIds", userId);
        mongoTemplate.updateFirst(query, update, MessageDO.class);
    }

    public MessageDO findByMsgId(String msgId) {
        return mongoTemplate.findOne(Query.query(Criteria.where("messageId").is(msgId)), MessageDO.class);
    }


    public boolean existsByMsgId(String msgId) {
        return messageRepository.existsByMessageId(msgId);
    }

    /**
     * 上一条消息
     * @param date
     * @return
     */
    public MessageDO prevMessage(String conversationId, Date date) {
        Query query = Query.query(Criteria.where("conversationId").is(conversationId).and("createTime").lt(date)).limit(1);
        return mongoTemplate.findOne(query, MessageDO.class);
    }

    /**
     * 是否最好一条消息
     * @param conversationId
     * @param date
     * @return
     */
    public boolean isLastMessage(String conversationId, String userId, Date date) {
        return !mongoTemplate.exists(Query.query(Criteria.where("conversationId")
                .is(conversationId).and("createTime").gt(date)
                .and("type").gte(0).and("deleteIds").ne(userId)), MessageDO.class);
    }

    public void delByMsgId(String msgId, String userId) {
        MessageDO messageDO = findByMsgId(msgId);
        boolean lastMsg = isLastMessage(messageDO.getConversationId(), userId, messageDO.getCreateTime());
        //最后一条消息要同步更新会话
        if (lastMsg) {
            MessageDO preMsg = prevMessage(messageDO.getConversationId(), messageDO.getCreateTime());
            if (preMsg != null) {
                conversationService.updateLastTime(messageDO.getConversationId(), userId, preMsg.getCreateTime(), preMsg.getType(), preMsg.getContent());
            }else
                conversationService.updateLastTime(messageDO.getConversationId(), userId, null, 0, "");
            topicProducer.sendNotifyMessage(new ResponseEvent(
                    new Response(
                            ResponseStatus.M_UPDATE_CONVERSATION,
                            Response.MESSAGE,
                            JSON.toJSONString(conversationService.findByConversationIdAndUserId(messageDO.getConversationId(), userId))),
                    userId));
        }

        if (messageDO.getUserId().equals(userId)) {
            //删除自己发送的消息
            updateMsgType(userId, msgId, -1);
            notifyAll(messageDO, new Response(ResponseStatus.M_DELETE_MESSAGE, Response.MESSAGE, JSON.toJSONString(messageDO)));

        }else {
            //删除对方的消息
            addToDeleteList(msgId, userId);
            topicProducer.sendNotifyMessage(new ResponseEvent(
                    new Response(ResponseStatus.M_DELETE_MESSAGE, Response.MESSAGE, JSON.toJSONString(messageDO)),
                    userId));
        }
    }

    /**
     *
    消息变动时通知自己和对方
     */
    public void notifyAll(MessageDO message, Response response) {
        if (message != null) {
            if(new Integer(0).equals(message.getChatType())) {
                //消息发送自己和对方
                ResponseEvent event = new ResponseEvent(response, Arrays.asList(message.getUserId(), message.getDestId()));
            }else if (new Integer(1).equals(message.getChatType())) {
                GroupChatDO groupChat = conversationService.findGroupChat(message.getDestId());
                List<String> userIds = groupChat.getMembers().stream().map(m -> m.getUserId()).collect(Collectors.toList());
                ResponseEvent event = new ResponseEvent(response, userIds);
                springUtils.getApplicationContext().publishEvent(event);
            }
        }
    }


}
