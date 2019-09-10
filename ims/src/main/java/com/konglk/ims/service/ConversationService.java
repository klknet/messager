package com.konglk.ims.service;

import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.comparator.ConversationComparator;
import com.konglk.ims.domain.*;
import com.konglk.ims.event.ResponseEvent;
import com.konglk.ims.event.TopicProducer;
import com.konglk.ims.util.SudokuGenerator;
import com.konglk.model.Response;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.konglk.model.ResponseStatus.U_GROUP_CHAT;

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
    @Autowired
    private RedisCacheService cacheService;
    @Autowired
    private TopicProducer topicProducer;

    @Value("${host}")
    String host;

    /*
    创建会话
     */
    public ConversationDO buildConversation(String userId, String destId) {
        ConversationDO conversationDO = getConversationDO(userId, destId);
        if (conversationDO == null) return null;
        this.mongoTemplate.insert(conversationDO);
        logger.info("build conversation {} {}", userId, destId);
        return conversationDO;
    }

    public void batchConversation(String userId, List<String> destIds) {
        List<ConversationDO> convs = new ArrayList<>(destIds.size());
        convs = destIds.stream().map(id -> getConversationDO(userId, id)).collect(Collectors.toList());
        mongoTemplate.insertAll(convs);
    }

    /*
    获取用户的会话列表
     */
    public List<ConversationDO> listConversation(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId)).with(Sort.by(Sort.Direction.DESC,
                "updateTime"));
        List<ConversationDO> convs = this.mongoTemplate.find(query, ConversationDO.class);
        if (CollectionUtils.isEmpty(convs))
            return Collections.emptyList();
        List<ConversationDO> tops = new ArrayList<>();
        List<ConversationDO> noTops = new ArrayList<>();
        List<ConversationDO> groupConvs = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        //置顶回话排在前面
        for (ConversationDO conv: convs) {
            ids.add(conv.getId());
            if (new Integer(1).equals(conv.getType()))
                groupConvs.add(conv);
            if (BooleanUtils.isTrue(conv.getTop()))
                tops.add(conv);
            else
                noTops.add(conv);
        }
        //未读消息
        Map<String, String> unreadNums = cacheService.getUnreadNum(userId);
        convs.forEach(conv -> {
            if (unreadNums.containsKey(conv.getConversationId())) {
                conv.setUnreadCount(Long.valueOf(unreadNums.get(conv.getConversationId())));
            }
        });
        //设置群聊成员
        if(groupConvs.size() > 0) {
            List<String> groupIds = groupConvs.stream().map(conv -> conv.getDestId()).collect(Collectors.toList());
            List<GroupChatDO> groupChats = findGroupChat(groupIds);
            Map<String, GroupChatDO> map = groupChats.stream().collect(Collectors.toMap(GroupChatDO::getId, Function.identity()));
            groupConvs.forEach(conv -> {
                if(map.containsKey(conv.getDestId()))
                    conv.setGroupChat(map.get(conv.getDestId()));
            });
        }

        if (tops.size() > 0) {
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

    /**
     * 更新会话时间，消息
     * @param conversationId
     * @param userId 如果为空，更新多方
     * @param date
     * @param type
     * @param msg
     */
    public void updateLastTime(String conversationId, String userId, Date date, Integer type, String msg) {
        //会话最后更新时间要晚于消息发送时间
        Criteria criteria = Criteria.where("conversationId").is(conversationId);
        if (StringUtils.isNotEmpty(userId))
            criteria.and("userId").is(userId);
        Query query = new Query(criteria);
        Update update = new Update();
        if(date != null) {
//            criteria.and("updateTime").lte(date);
            update.set("updateTime", date);
        }
        if (type != null)
            update.set("messageType", type);
        update.set("lastMsg", msg);
        mongoTemplate.updateMulti(query, update, ConversationDO.class);
    }


    /*
    是否存在会话
     */
    public boolean existConversation(String userId, String destId) {
        Query query = Query.query(Criteria.where("userId").is(userId).and("destId").is(destId));
        return mongoTemplate.exists(query, ConversationDO.class);
    }

    /*
    更新会话名称
     */
    public void updateConversationName(String userId, String destId, String notename) {
        Query query = Query.query(Criteria.where("user_id").is(userId).and("dest_id").is(destId));
        Update update = Update.update("notename", notename);
        mongoTemplate.updateFirst(query, update, ConversationDO.class);
    }

    /*
    加入到会议
     */
    public void joinConversation(String userId, String destId, String conversationId, Date createtime, Integer type) {
        ConversationDO conversationDO = new ConversationDO();
        UserDO friend = userService.findByUserId(destId);
        UserDO userDO = userService.findByUserId(userId);
        if (userDO.getFriends() != null) {
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
        conversationDO.setType(type);
        mongoTemplate.insert(conversationDO);
    }

    public ConversationDO findByUserIdAndDestId(String userId, String destId) {
        ConversationDO conversationDO = this.mongoTemplate.findOne(new Query(new Criteria()
                .andOperator(new Criteria[]{Criteria.where("userId").is(userId), Criteria.where("destId").is(destId)})), ConversationDO.class);
        return conversationDO;
    }

    public ConversationDO findByConversationIdAndUserId(String conversationId, String userId) {
        return mongoTemplate.findOne(Query.query(
                Criteria.where("conversationId").is(conversationId).and("userId").is(userId)),
                ConversationDO.class);
    }

    /*
    查询群聊成员
     */
    public GroupChatDO findGroupChat(String id) {
        if (StringUtils.isEmpty(id))
            return null;
        return mongoTemplate.findById(id, GroupChatDO.class);
    }

    /*
    查询多个id
     */
    public List<GroupChatDO> findGroupChat(List<String> id) {
        if(CollectionUtils.isEmpty(id))
            return Collections.emptyList();
        if(id.size() == 1) {
            GroupChatDO groupChat = findGroupChat(id.get(0));
            return groupChat == null ? Collections.emptyList() : Arrays.asList(groupChat);
        }
        return mongoTemplate.find(Query.query(Criteria.where("_id").in(id)), GroupChatDO.class);
    }

    public void updateConversation(MessageDO messageDO) {
        ConversationDO conv = findByUserIdAndDestId(messageDO.getUserId(), messageDO.getDestId());
        if (new Integer(0).equals(conv.getType())) {
            // 如果一方没有会话，则创建会话
            if (!this.existConversation(messageDO.getDestId(), messageDO.getUserId())) {
                this.joinConversation(messageDO.getDestId(), messageDO.getUserId(),
                        messageDO.getConversationId(), messageDO.getCreateTime(), messageDO.getChatType());
            }
            this.updateLastTime(messageDO.getConversationId(), null, messageDO.getCreateTime(), messageDO.getType(), messageDO.getContent());
        } else {
            updateGroupChat(conv.getDestId(), messageDO.getType(), messageDO.getContent());
        }
    }

    /*
    更新群聊消息
     */
    public void updateGroupChat(String destId, int type, String content) {
        Query query = new Query(Criteria.where("dest_id").is(destId));
        Update update = new Update();
        update.set("updateTime", new Date());
        update.set("messageType", type);
        update.set("lastMsg", content);
        mongoTemplate.updateMulti(query, update, ConversationDO.class);
    }

    /*
    群聊
     */
    public void groupConversation(String userId, List<String> userIds, String notename) throws IOException {
        if (StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(userIds))
            return;
        GroupChatDO groupChatDO = new GroupChatDO();
        List<UserDO> users = userService.findUsers(userIds.toArray(new String[userIds.size()]));
        List<String> avatars = new ArrayList<>(userIds.size()); //头像
        List<GroupChatDO.Member> members = users.stream().map(user -> {
            GroupChatDO.Member member = new GroupChatDO.Member(user.getUserId(), user.getProfileUrl(),
                    user.getNickname() == null ? user.getUsername() : user.getNickname());
            if (user.getProfileUrl().startsWith("http")) {
                avatars.add(user.getProfileUrl());
            }else {
                avatars.add("http://127.0.0.1/ims/file/img?id="+user.getProfileUrl());
            }
            return member;
        }).collect(Collectors.toList());
        groupChatDO.setMembers(members);
        mongoTemplate.insert(groupChatDO);
        //生成九宫格头像
        BufferedImage image = sudokuGenerator.clipImages(avatars.size()>9 ? avatars.subList(0, 9).toArray(new String[9])
                : avatars.toArray(new String[avatars.size()]));
        //将图片存入gridfs
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "JPG", outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        DBObject metadata = new BasicDBObject();
        metadata.put("group_id", groupChatDO.getId());
        ObjectId objectId = gridFsTemplate.store(inputStream, groupChatDO.getId(), "image/jpg", metadata);

        Date date = new Date();
        String convId = UUID.randomUUID().toString();
        for (String uId : userIds) {
            ConversationDO conv = new ConversationDO();
            conv.setUserId(uId);
            conv.setDestId(groupChatDO.getId());
            conv.setCreateTime(date);
            conv.setUpdateTime(date);
            conv.setConversationId(convId);
            conv.setNotename(notename);
            conv.setType(1);
            conv.setProfileUrl(objectId.toString());
            mongoTemplate.insert(conv);
        }
        //通知群聊会话已创建
        Response response = new Response(U_GROUP_CHAT, Response.USER);
        ResponseEvent event = new ResponseEvent(response, userIds);
        topicProducer.sendNotifyMessage(event);
    }

    /**
     * 更新好友会话头像
     * @param userId
     * @param profileUrl
     */
    public void updateConvProfile(String userId, String profileUrl) {
        Query query = new Query(Criteria.where("destId").is(userId));
        Update update = Update.update("profileUrl", profileUrl);
        mongoTemplate.updateMulti(query, update, ConversationDO.class);
    }

    private ConversationDO getConversationDO(String userId, String destId) {
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

        if(friend == null || userDO == null) {
            throw new IllegalArgumentException();
        }

        if (userDO.getFriends() != null) {
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
        conversationDO.setType(0);
        return conversationDO;
    }


}
