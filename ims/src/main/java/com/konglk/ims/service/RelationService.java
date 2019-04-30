package com.konglk.ims.service;

import com.konglk.ims.domain.FriendDO;
import com.konglk.ims.domain.FriendRequestDO;
import com.konglk.ims.domain.UserDO;
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
    }

    /**
    同意添加
     */
    public void agreeRequest(String userId, String destId) {
        updateStatus(userId, destId, 1);
        // 添加朋友
        userService.addFriend(destId, userId, null);
        logger.info("{} agree friend request of {}", destId, userId);
    }

    /*
    拒绝添加
     */
    public void refuseRequest(String userId, String destId) {
        updateStatus(userId, destId, 2);
        logger.info("{} refuse friend request of {}", destId, userId);
    }

    /*
    删除请求
     */
    public void delRequest(String userId, String destId) {
        Query query = new Query(Criteria.where("destId").is(destId).and("userId").is(userId));
        mongoTemplate.findAndRemove(query, FriendRequestDO.class);
        logger.info("{} delete friend request of {}", destId, userId);
    }

    public List<FriendRequestDO> requestList(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        return mongoTemplate.find(query, FriendRequestDO.class);
    }

    private void updateStatus(String userId, String destId, int status) {
        Query query = new Query(Criteria.where("destId").is(destId).and("userId").is(userId));
        Update update = new Update();
        update.set("status", status);
        mongoTemplate.findAndModify(query, update, FriendRequestDO.class);
    }
}
