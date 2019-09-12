package com.konglk.ims.service;

import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.comparator.ConversationComparator;
import com.konglk.ims.domain.ConversationDO;
import com.konglk.ims.domain.GroupChatDO;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.domain.UserDO;
import com.konglk.ims.event.ResponseEvent;
import com.konglk.ims.event.TopicProducer;
import com.konglk.ims.repo.IConversationRepository;
import com.konglk.ims.repo.IGroupChatRepository;
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
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.konglk.model.ResponseStatus.U_GROUP_CHAT;

@Service
public class ConversationService {

    private Logger logger = LoggerFactory.getLogger(getClass());

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
    @Autowired
    private IConversationRepository conversationRepository;
    @Autowired
    private IGroupChatRepository IGroupChatRepository;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /*
    创建会话
     */
    @Transactional
    public ConversationDO buildConversation(String userId, String destId) {
        ConversationDO conversationDO = getConversationDO(userId, destId);
        if (conversationDO == null) return null;
        conversationRepository.save(conversationDO);
        logger.info("build conversation {} {}", userId, destId);
        return conversationDO;
    }

    @Transactional
    public void batchConversation(String userId, List<String> destIds) {
        List<ConversationDO> convs = new ArrayList<>(destIds.size());
        convs = destIds.stream().map(id -> getConversationDO(userId, id)).collect(Collectors.toList());
        conversationRepository.saveAll(convs);
    }

    /*
    获取用户的会话列表
     */
    public List<ConversationDO> listConversation(String userId) {
        List<ConversationDO> convs = conversationRepository.findByUserIdOrderByCreateTimeDesc(userId);
        if (CollectionUtils.isEmpty(convs))
            return Collections.emptyList();
        List<ConversationDO> tops = new ArrayList<>();
        List<ConversationDO> noTops = new ArrayList<>();
        List<ConversationDO> groupConvs = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        //置顶回话排在前面
        for (ConversationDO conv: convs) {
            ids.add(conv.getConversationId());
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
            Map<String, List<GroupChatDO>> map = groupChats.stream().collect(Collectors.groupingBy(GroupChatDO::getGroupId));
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
    @Transactional
    public void topConversation(String userId, String conversationId, boolean top) {
        conversationRepository.topConversation(conversationId, userId, top, new Date());
    }

    /*
    消息免打扰
     */
    @Transactional
    public void dndConversation(String userId, String conversationId, boolean dnd) {
        conversationRepository.dndConversation(conversationId, userId, dnd);
    }

    /*
    删除会话
     */
    /**
     * 更新会话时间，消息
     * @param conversationId
     * @param userId 如果为空，更新多方
     * @param date
     * @param type
     * @param msg
     */
    @Transactional
    public void updateLastTime(String conversationId, String userId, Date date, Integer type, String msg) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        //会话最后更新时间要晚于消息发送时间
        String sql = "update im_conversation set last_msg=:lastMsg";
        params.addValue("lastMsg", msg);
        if(date != null) {
            sql += ",update_time=:updateTime";
            params.addValue("updateTime", date);
        }
        if (type != null) {
            sql += ",message_type=:messageType";
            params.addValue("messageType", type);
        }
        sql += " where conversation_id=:conversationId";
        params.addValue("conversationId", conversationId);
        if (StringUtils.isNotEmpty(userId)) {
            sql += " and user_id=:userId";
            params.addValue("userId", userId);
        }
        jdbcTemplate.update(sql, params);
    }

    @Transactional
    public void delete(String conversationId, String userId) {
        conversationRepository.removeByConversationIdAndUserId(conversationId, userId);
    }


    /*
    是否存在会话
     */
    public boolean existConversation(String userId, String destId) {
        return conversationRepository.existsByUserIdAndDestId(userId, destId);
    }

    /*
    更新会话名称
     */
    @Transactional
    public void updateConversationName(String userId, String destId, String notename) {
       conversationRepository.updateConversationName(userId, destId, notename);
    }

    /*
    加入到会议
     */
    public void joinConversation(String userId, String destId, String conversationId, Date createtime, Integer type) {
        ConversationDO conversationDO = new ConversationDO();
        UserDO friend = userService.findByUserId(destId);
        conversationDO.setNotename(friend.getNickname());
        conversationDO.setUserId(userId);
        conversationDO.setDestId(destId);
        conversationDO.setProfileUrl(friend.getProfileUrl());
        conversationDO.setConversationId(conversationId);
        conversationDO.setCreateTime(createtime);
        conversationDO.setUpdateTime(createtime);
        conversationDO.setType(type);
        conversationRepository.save(conversationDO);
    }

    public ConversationDO findByUserIdAndDestId(String userId, String destId) {
        return conversationRepository.findByUserIdAndDestId(userId, destId);
    }

    public ConversationDO findByConversationIdAndUserId(String conversationId, String userId) {
        return conversationRepository.findByConversationIdAndUserId(conversationId, userId);
    }


    /*
    查询多个id
     */
    public List<GroupChatDO> findGroupChat(List<String> id) {
        if(CollectionUtils.isEmpty(id))
            return Collections.emptyList();
        if(id.size() == 1) {
            return IGroupChatRepository.findByGroupId(id.get(0));
        }
        return IGroupChatRepository.findByIdIn(id);
    }

    /*
    查询多个id
     */
    public List<GroupChatDO> findGroupChat(String id) {
        if(StringUtils.isEmpty(id))
            return Collections.emptyList();
        return IGroupChatRepository.findByGroupId(id);
    }

    @Transactional
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
    @Transactional
    public void updateGroupChat(String destId, int type, String content) {
        conversationRepository.updateConversationInfo(destId, new Date(), type, content);
    }

    /*
    群聊
     */
    @Transactional
    public void groupConversation(String userId, List<String> userIds, String notename) throws IOException {
        if (StringUtils.isEmpty(userId) || CollectionUtils.isEmpty(userIds))
            return;
        List<UserDO> users = userService.findUsers(userIds.toArray(new String[userIds.size()]));
        List<String> avatars = new ArrayList<>(userIds.size()); //头像
        String uuid = UUID.randomUUID().toString();
        List<GroupChatDO> members = users.stream().map(user -> {
            GroupChatDO member = new GroupChatDO(uuid, user.getUserId(), user.getProfileUrl(),
                    user.getNickname() == null ? user.getUsername() : user.getNickname());
            if (user.getProfileUrl().startsWith("http")) {
                avatars.add(user.getProfileUrl());
            }else {
                avatars.add("http://127.0.0.1/ims/file/img?id="+user.getProfileUrl());
            }
            return member;
        }).collect(Collectors.toList());
        IGroupChatRepository.saveAll(members);
        //生成九宫格头像
        BufferedImage image = sudokuGenerator.clipImages(avatars.size()>9 ? avatars.subList(0, 9).toArray(new String[9])
                : avatars.toArray(new String[avatars.size()]));
        //将图片存入gridfs
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "JPG", outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        DBObject metadata = new BasicDBObject();
        metadata.put("group_id", uuid);
        ObjectId objectId = gridFsTemplate.store(inputStream, uuid, "image/jpg", metadata);

        Date date = new Date();
        String convId = UUID.randomUUID().toString();
        List<ConversationDO> convs = new ArrayList<>();
        for (String uId : userIds) {
            ConversationDO conv = new ConversationDO();
            conv.setUserId(uId);
            conv.setDestId(uuid);
            conv.setCreateTime(date);
            conv.setUpdateTime(date);
            conv.setConversationId(convId);
            conv.setNotename(notename);
            conv.setType(1);
            conv.setProfileUrl(objectId.toString());
            convs.add(conv);
        }
        conversationRepository.saveAll(convs);
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
        conversationRepository.updateAvatar(userId, profileUrl);
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
        conversationDO.setNotename(friend.getNickname());
        conversationDO.setProfileUrl(friend.getProfileUrl());
        conversationDO.setDestId(destId);
        conversationDO.setCreateTime(new Date());
        conversationDO.setUpdateTime(new Date());
        conversationDO.setUserId(userId);
        conversationDO.setType(0);
        return conversationDO;
    }


}
