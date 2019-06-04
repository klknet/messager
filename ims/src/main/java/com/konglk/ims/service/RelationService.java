package com.konglk.ims.service;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.domain.FriendDO;
import com.konglk.ims.domain.FriendRequestDO;
import com.konglk.ims.domain.UserDO;
import com.konglk.ims.ws.ConnectionHolder;
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
    private ReplyService replyService;
    @Autowired
    private ConnectionHolder connectionHolder;

    /*
    请求添加朋友
     */
    public void requestFriend(String userId, String destId, String note) {
        UserDO userDO = userService.findByUserId(userId);
        if(userDO == null)
            throw new IllegalArgumentException("userId not exists!");
        if(userDO.getFriends() != null) {
            for(FriendDO friendDO: userDO.getFriends()) {
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
        replyService.replyRequestFriend(connectionHolder.getClient(destId), JSON.toJSONString(requestDO));
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
        replyService.replyAgreeFriend(connectionHolder.getClient(friendRequestDO.getUserId()));
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
