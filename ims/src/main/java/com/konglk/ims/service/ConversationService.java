package com.konglk.ims.service;

import com.konglk.ims.comparator.ConversationComparator;
import com.konglk.ims.domain.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.konglk.ims.util.SudokuGenerator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;

@Service
public class ConversationService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private SudokuGenerator sudokuGenerator;
    @Autowired
    private GridFsTemplate gridFsTemplate;

    /*
    创建会话
     */
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

    /*
    获取用户的会话列表
     */
    public List<ConversationDO> listConversation(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId)).with(Sort.by(Sort.Direction.DESC,
              "updateTime"));
        List<ConversationDO> convs = this.mongoTemplate.find(query, ConversationDO.class);
        if(CollectionUtils.isEmpty(convs))
            return Collections.emptyList();
        List<ConversationDO> tops = new ArrayList<>();
        List<ConversationDO> noTops = new ArrayList<>();
        //置顶回话排在前面
        for(ConversationDO conv: convs) {
            if (BooleanUtils.isTrue(conv.getTop()))
                tops.add(conv);
            else
                noTops.add(conv);
        }
        if(tops.size() > 0) {
            //多个置顶按照置顶时间倒排
            tops.sort(ConversationComparator.compareUpdateTime());
            List<ConversationDO> result = new ArrayList<>(convs.size());
            result.addAll(tops);
            result.addAll(noTops);
            return result;
        }
        return convs;
    }

    /*
    消息置顶
     */
    public void topConversation(String userId, String conversationId, boolean top) {
        Query query = new Query(Criteria.where("conversation_id").is(conversationId).and("userId").is(userId));
        Update update = Update.update("top", top);
        update.set("topUpdateTime", new Date());
        mongoTemplate.updateFirst(query, update, ConversationDO.class);
    }

    /*
    消息免打扰
     */
    public void dndConversation(String userId, String conversationId, boolean dnd) {
        Query query = new Query(Criteria.where("conversation_id").is(conversationId).and("userId").is(userId));
        Update update = Update.update("dnd", dnd);
        mongoTemplate.updateFirst(query, update, ConversationDO.class);
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

    /*
    群聊
     */
    public ConversationDO groupConversation(String userId, List<String> userIds, String notename) throws IOException {
        if(StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(userIds))
            return null;
        GroupChatDO groupChatDO = new GroupChatDO();
        List<UserDO> users = userService.findUsers(userIds.toArray(new String[userIds.size()]));
        List<String> avatars = new ArrayList<>(userIds.size()); //头像
        List<GroupChatDO.Member> members = users.stream().map(user -> {
            GroupChatDO.Member member = new GroupChatDO.Member(user.getUserId(),
                    user.getNickname() == null ? user.getUsername() : user.getNickname(),user.getProfileUrl());
            avatars.add(user.getProfileUrl());
            return member;
        }).collect(Collectors.toList());
        groupChatDO.setMembers(members);
        mongoTemplate.insert(groupChatDO);

        ConversationDO conv = new ConversationDO();
        conv.setUserId(userId);
        conv.setDestId(groupChatDO.getId());
        conv.setCreateTime(new Date());
        conv.setUpdateTime(new Date());
        conv.setConversationId(UUID.randomUUID().toString());
        conv.setNotename(notename);
        conv.setType(1);
        //生成九宫格头像
        BufferedImage image = sudokuGenerator.clipImages(avatars.toArray(new String[avatars.size()]));
        //将图片存入gridfs
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "JPG", outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        DBObject metadata = new BasicDBObject();
        metadata.put("group_id", groupChatDO.getId());
        ObjectId objectId = gridFsTemplate.store(inputStream, groupChatDO.getId(), "image/jpg", metadata);
        conv.setProfileUrl(objectId.toString());
        mongoTemplate.insert(conv);
        logger.info("build group conversation {} {}", userId, notename);
        return conv;
    }

}
