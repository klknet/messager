package com.konglk.ims.service;

import com.konglk.ims.domain.ConversationDO;
import com.konglk.ims.domain.FriendDO;
import com.konglk.ims.domain.UserDO;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ConversationService
{
  @Autowired
  private MongoTemplate mongoTemplate;
  
  public void buildConversation(String userId, String destId)
  {
    ConversationDO one = (ConversationDO)this.mongoTemplate.findOne(new Query(new Criteria()
      .andOperator(new Criteria[] {Criteria.where("userId").is(userId), Criteria.where("destId").is(destId) })), ConversationDO.class);
    if (one != null) {
      return;
    }
    UserDO friend = (UserDO)this.mongoTemplate.findOne(new Query()
      .addCriteria(Criteria.where("userId").is(destId)), UserDO.class);
    UserDO userDO = (UserDO)this.mongoTemplate.findOne(new Query()
      .addCriteria(Criteria.where("userId").is(userId)), UserDO.class);
    ConversationDO conversationDO = new ConversationDO();
    for (FriendDO friendDO : userDO.getFriends()) {
      if (friendDO.getUserId().equals(friend.getUserId()))
      {
        conversationDO.setNotename(friendDO.getRemark());
        break;
      }
    }
    conversationDO.setProfileUrl(friend.getProfileUrl());
    conversationDO.setConversationId(UUID.randomUUID().toString());
    conversationDO.setDestId(destId);
    conversationDO.setCreateTime(new Date());
    conversationDO.setUpdateTime(new Date());
    conversationDO.setUserId(userId);
    this.mongoTemplate.insert(conversationDO);
  }
  
  public List<ConversationDO> listConversation(String userId)
  {
    Query query = new Query(Criteria.where("userId").is(userId)).with(Sort.by(Sort.Direction.DESC, new String[] { "updateTime" }));
    return this.mongoTemplate.find(query, ConversationDO.class);
  }
}
