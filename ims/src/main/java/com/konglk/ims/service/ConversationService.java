package com.konglk.ims.service;

import com.konglk.ims.domain.ConversationDO;
import com.konglk.ims.domain.FriendDO;
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
import org.springframework.stereotype.Service;

@Service
public class ConversationService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    public ConversationDO buildConversation(String userId, String destId) {
        ConversationDO one = this.mongoTemplate.findOne(new Query(new Criteria()
                .andOperator(new Criteria[]{Criteria.where("userId").is(userId), Criteria.where("destId").is(destId)})), ConversationDO.class);
        if (one != null) {
            return null;
        }
        ConversationDO other = this.mongoTemplate.findOne(new Query(new Criteria()
                .andOperator(new Criteria[]{Criteria.where("userId").is(destId), Criteria.where("destId").is(userId)})), ConversationDO.class);
        ConversationDO conversationDO = new ConversationDO();
        //如果一方会话已存在，加入到该会话，否则重新建立会话
        if (other != null) {
            conversationDO.setConversationId(other.getConversationId());
        } else {
            conversationDO.setConversationId(UUID.randomUUID().toString());
        }
        UserDO friend = this.mongoTemplate.findOne(new Query()
                .addCriteria(Criteria.where("userId").is(destId)), UserDO.class);
        UserDO userDO = this.mongoTemplate.findOne(new Query()
                .addCriteria(Criteria.where("userId").is(userId)), UserDO.class);

        for (FriendDO friendDO : userDO.getFriends()) {
            if (friendDO.getUserId().equals(friend.getUserId())) {
                conversationDO.setNotename(friendDO.getRemark());
                break;
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

    public void delete(String conversationId, String userId) {
        Query query = new Query(Criteria.where("conversationId").is(conversationId).and("userId").is(userId));
        this.mongoTemplate.remove(query, ConversationDO.class);
        logger.info("delete conversation {} {}", conversationId, userId);
    }
}
