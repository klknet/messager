package com.konglk.ims.service;

import com.konglk.ims.controller.UserController;
import com.konglk.ims.domain.FriendDO;
import com.konglk.ims.domain.FriendRequestDO;
import com.konglk.ims.domain.UserDO;
import com.konglk.ims.event.ResponseEvent;
import com.konglk.ims.event.TopicProducer;
import com.konglk.ims.repo.IFriendRepository;
import com.konglk.ims.repo.IUserRepository;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by konglk on 2019/4/23.
 */
@Service
public class RelationService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private TopicProducer topicProducer;
    @Autowired
    private IFriendRepository friendRepository;
    @Autowired
    private IUserRepository userRepository;

    /*
    请求添加朋友
     */
    public void requestFriend(String userId, String destId, String note) {
        UserDO userDO = userRepository.findByUserId(userId);
        if(userDO == null)
            throw new IllegalArgumentException("userId not exists!");
        List<FriendDO> friendDOS = friendRepository.findByUserId(userId);
        if(friendDOS != null) {
            for(FriendDO friendDO: friendDOS) {
                if (friendDO.getUserId().equals(destId)) {
                    throw new IllegalArgumentException("destId was friend");
                }
            }
        }
        FriendRequestDO requestDO = new FriendRequestDO();
        requestDO.setCreatetime(new Date());
        requestDO.setDestId(destId);
        requestDO.setUserId(userId);
        requestDO.setProfileUrl(userDO.getProfileUrl());
        requestDO.setNote(note);
        requestDO.setStatus(0);
        requestDO.setUsername(userDO.getUsername());
        mongoTemplate.insert(requestDO);
        logger.info("{} add friend request to {}", userDO.getNickname(), destId);

        ResponseEvent event = new ResponseEvent(new Response(ResponseStatus.U_FRIEND_REQUEST, Response.USER), destId);
        topicProducer.sendNotifyMessage(event);
    }

    /**
    同意添加
     */
    public void agreeRequest(String objectId, String userId) {
        FriendRequestDO friendRequestDO = mongoTemplate.findById(objectId, FriendRequestDO.class);
        if(friendRequestDO == null || !friendRequestDO.getDestId().equals(userId))
            return;
        updateStatus(objectId, 1);
        // 添加朋友
        userService.addFriend(friendRequestDO.getUserId(), friendRequestDO.getDestId(), null);
        userService.addFriend(friendRequestDO.getDestId(), friendRequestDO.getUserId(), null);
        logger.info("{} agree friend request of {}", friendRequestDO.getDestId(), friendRequestDO.getUserId());
        //通知客户端刷新朋友列表
        ResponseEvent event = new ResponseEvent(new Response(ResponseStatus.U_AGREE_FRIEND_REQUEST, Response.USER), friendRequestDO.getUserId());
        topicProducer.sendNotifyMessage(event);

    }

    /*
    拒绝添加
     */
    public void refuseRequest(String objectId, String userId) {
        FriendRequestDO friendRequestDO = mongoTemplate.findById(objectId, FriendRequestDO.class);
        if(friendRequestDO == null || !friendRequestDO.getDestId().equals(userId))
            return;
        updateStatus(objectId, 2);
        logger.info("{} refuse friend request of {}", friendRequestDO.getDestId(), friendRequestDO.getUserId());
    }

    /*
    删除请求
     */
    public void delRequest(String objectId) {
        FriendRequestDO friendRequestDO = mongoTemplate.findById(objectId, FriendRequestDO.class);
        if (friendRequestDO == null) {
            return;
        }
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(objectId)));
        logger.info("{} delete friend request of {}", friendRequestDO.getDestId(), friendRequestDO.getUserId());
    }

    /*
    好友请求列表
     */
    public List<FriendRequestDO> requestList(String userId) {
        Query query = new Query(Criteria.where("destId").is(userId));
        return mongoTemplate.find(query, FriendRequestDO.class);
    }

    /*
    更新请求状态
     */
    private void updateStatus(String objectId, int status) {
        Query query = new Query(Criteria.where("_id").is(objectId));
        Update update = new Update();
        update.set("status", status);
        mongoTemplate.findAndModify(query, update, FriendRequestDO.class);
    }

}
