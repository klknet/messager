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
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private TopicProducer topicProducer;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     *
     * @param cid
     * @param userId
     * @param start  会话创建时间
     * @param end 早于此时间的消息
     * @param includeStart  是否包含当前时间点的数据
     * @return
     */
    public List<MessageDO> prevMessages(String cid, String userId, Date start, Date end, boolean includeStart) {
        String sql = "select * from im_message where";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("start", start);
        if (includeStart) {
            sql += " create_time>=:start";
        }else {
            sql += " create_time>:start";
        }
        if (end != null) {
            params.addValue("end", end);
            sql += " and create_time<:end";
        }
        params.addValue("cid", cid);
        sql += " and conversation_id=:cid and type>=0 and (delete_ids is null or delete_ids not like :deleteIds)";
        sql += " order by create_time desc limit 0, 32";
        params.addValue("deleteIds", userId+"%");
        List<MessageDO> messageDOS = jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(MessageDO.class));
        messageDOS.forEach(m -> {
            if (new Integer(2).equals(m.getType())) {
                GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(m.getContent())));
                m.setFileDetail(new FileDetail(gridFSFile.getLength(), gridFSFile.getFilename(), gridFSFile.getMetadata().getString("_contentType")));
            }
        });
        return messageDOS;
    }

    @Transactional
    public void insert(MessageDO messageDO) {
        if(messageDO.getCreateTime() == null) {
            messageDO.setCreateTime(new Date());
        }
        messageRepository.save(messageDO);
    }

    public void failedMessage(FailedMessageDO msg) {
        mongoTemplate.insert(msg);
    }

    /*
    撤回消息
     */
    @Transactional
    public void revocation(String userId, String msgId) {
        MessageDO message = messageRepository.findByMessageIdAndUserId(msgId, userId);
        if (msgId == null || !userId.equals(message.getUserId()))
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
    @Transactional
    public void updateMsgType(String userId, String msgId, int type) {
        messageRepository.updateMsgType(msgId, userId, type);
    }

    /**
     * 该条消息对指定用户删除
     * @param msgId
     * @param userId
     */
    @Transactional
    public void addToDeleteList(String msgId, String userId) {
        MessageDO messageDO = messageRepository.findByMessageId(msgId);
        if (messageDO == null)
            return;
        String deleteIds = messageDO.getDeleteIds();
        if (StringUtils.isEmpty(deleteIds))
            deleteIds = userId;
        else {
            Set<String> unique = new TreeSet<>(Arrays.asList(deleteIds.split(",")));
            unique.add(userId);
            deleteIds = StringUtils.join(unique, ",");
        }
        messageRepository.updateDeleteIds(msgId, userId, deleteIds);
    }

    public MessageDO findByMsgId(String msgId) {
        return messageRepository.findByMessageId(msgId);
    }


    public boolean existsByMsgId(String msgId) {
        return messageRepository.existsByMessageId(msgId);
    }


    /**
     * 是否最后一条消息
     * @param conversationId
     * @param date
     * @return
     */
    public boolean isLastMessage(String conversationId, String userId, Date date) {
        return messageRepository.isLastMsg(conversationId, userId+"%", date) == 1L;
    }

    @Transactional
    public void delByMsgId(String msgId, String userId) {
        MessageDO messageDO = findByMsgId(msgId);
        boolean lastMsg = isLastMessage(messageDO.getConversationId(), userId, messageDO.getCreateTime());
        //最后一条消息要同步更新会话
        if (lastMsg) {
            MessageDO preMsg = messageRepository.prevMessage(messageDO.getConversationId(), userId, messageDO.getCreateTime());
            if (preMsg != null) {
                conversationService.updateLastTime(messageDO.getConversationId(), userId, preMsg.getCreateTime(), preMsg.getType(), preMsg.getContent());
            }else
                conversationService.updateLastTime(messageDO.getConversationId(), userId, null, 0, "删除了一条消息");
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
                springUtils.getApplicationContext().publishEvent(event);
            }else if (new Integer(1).equals(message.getChatType())) {
                List<GroupChatDO> groupChat = conversationService.findGroupChat(message.getDestId());
                List<String> userIds = groupChat.stream().map(m -> m.getUserId()).collect(Collectors.toList());
                ResponseEvent event = new ResponseEvent(response, userIds);
                springUtils.getApplicationContext().publishEvent(event);
            }
        }
    }


}
