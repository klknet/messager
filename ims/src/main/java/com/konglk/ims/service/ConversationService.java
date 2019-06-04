package com.konglk.ims.service;

import com.konglk.ims.domain.ConversationDO;
import com.konglk.ims.domain.FriendDO;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.domain.UserDO;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserService userService;

    public ConversationDO buildConversation(String userId, String destId) {
        if (this.existConversation(userId, destId)) {
            logger.warn("conversation already exist!");
            return null;
        }
        ConversationDO other = this.findByUserIdAndDestId(destId, userId);
        ConversationDO conversationDO = new ConversationDO();
        //如果一方会话已存在，加入到该会话，否则重新建立会话
        if (other != null) {
            conversationDO.setConversationId(other.getConversationId());
        } else {
            conversationDO.setConversationId(UUID.randomUUID().toString());
        }
        UserDO friend = userService.findByUserId(destId);
        UserDO userDO = userService.findByUserId(userId);

        if(userDO.getFriends() != null) {
            for (FriendDO friendDO : userDO.getFriends()) {
                if (friendDO.getUserId().equals(friend.getUserId())) {
                    conversationDO.setNotename(friendDO.getRemark());
                    break;
                }
            }
        }
        conversationDO.setProfileUrl(friend.getProfileUrl());

        conversationDO.setDestId(destId);
        conversationDO.setCreateTime(new Date());
        conversationDO.setUpdateTime(new Date());
        conversationDO.setUserId(userId);
        this.mongoTemplate.insert(conversationDO);
        logger.info("build conversation {} {}", userId, destId);
        return conversationDO;
    }

    public List<ConversationDO> listConversation(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId)).with(Sort.by(Sort.Direction.DESC,
                new String[]{"updateTime"}));
        return this.mongoTemplate.find(query, ConversationDO.class);
    }

    /*
    删除会话
     */
    public void delete(String conversationId, String userId) {
        Query query = new Query(Criteria.where("conversationId").is(conversationId).and("userId").is(userId));
        this.mongoTemplate.remove(query, ConversationDO.class);
        logger.info("delete conversation {} {}", conversationId, userId);
    }

    /*
    更新会话时间，消息
     */
    public void updateLastTime(String conversationId, String userId, Date date, int type, String msg) {
        Query query = new Query(Criteria.where("conversationId").is(conversationId).and("userId").is(userId)
                .and("updateTime").lte(date));
        Update update = new Update();
        update.set("updateTime", date);
        update.set("type", type);
        update.set("lastMsg", msg);
        mongoTemplate.updateFirst(query, update, ConversationDO.class);
    }

    /*
    是否存在会话
     */
    public boolean existConversation(String userId, String destId) {
        Query query =Query.query(Criteria.where("userId").is(userId).and("destId").is(destId));
        return mongoTemplate.exists(query, ConversationDO.class);
    }

    public void joinConversation(String userId, String destId, String conversationId, Date createtime) {
        ConversationDO conversationDO = new ConversationDO();
        UserDO friend = userService.findByUserId(destId);
        UserDO userDO = userService.findByUserId(userId);
        if(userDO.getFriends() != null) {
            for (FriendDO friendDO : userDO.getFriends()) {
                if (friendDO.getUserId().equals(friend.getUserId())) {
                    conversationDO.setNotename(friendDO.getRemark());
                    break;
                }
            }
        }
        conversationDO.setUserId(userId);
        conversationDO.setDestId(destId);
        conversationDO.setProfileUrl(friend.getProfileUrl());
        conversationDO.setConversationId(conversationId);
        conversationDO.setCreateTime(createtime);
        conversationDO.setUpdateTime(createtime);
        mongoTemplate.insert(conversationDO);
    }

    public ConversationDO findByUserIdAndDestId(String userId, String destId) {
        ConversationDO conversationDO = this.mongoTemplate.findOne(new Query(new Criteria()
                .andOperator(new Criteria[]{Criteria.where("userId").is(userId), Criteria.where("destId").is(destId)})), ConversationDO.class);
        return conversationDO;
    }

    public void updateConversation(MessageDO messageDO) {
        // 如果一方没有会话，则创建会话
        if (! this.existConversation(messageDO.getDestId(), messageDO.getUserId())) {
            this.joinConversation(messageDO.getDestId(), messageDO.getUserId(),
                    messageDO.getConversationId(), messageDO.getCreateTime());
        }
        this.updateLastTime(messageDO.getConversationId(), messageDO.getUserId(),
                messageDO.getCreateTime(), messageDO.getType(), messageDO.getContent());
        this.updateLastTime(messageDO.getConversationId(), messageDO.getDestId(),
                messageDO.getCreateTime(), messageDO.getType(), messageDO.getContent());
    }

    public void groupConversation(String userId, List<String> userIds) {

    }

}
