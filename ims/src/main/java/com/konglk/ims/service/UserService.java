package com.konglk.ims.service;

import com.konglk.ims.domain.FriendDO;
import com.konglk.ims.domain.UserDO;
import com.konglk.ims.util.DecodeUtils;

import java.util.Date;
import java.util.UUID;

import com.konglk.ims.ws.ChatClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SALT = "konglk";
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ChatClient chatClient;

    public UserDO login(String unique, String pwd) {
        String raw = DecodeUtils.decode(pwd, "konglk");
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where("username").is(unique),
                Criteria.where("cellphone").is(unique),
                Criteria.where("mailbox").is(unique)));
        UserDO userDO = (UserDO) this.mongoTemplate.findOne(query, UserDO.class);
        if (userDO == null) {
            return null;
        }
        raw = DigestUtils.md5DigestAsHex((raw + userDO.getSalt()).getBytes());
        if (StringUtils.equals(raw, userDO.getRawPwd())) {
            //登录凭证
            String ticket = UUID.randomUUID().toString();
            chatClient.addTicket(userDO.getUserId(), ticket);
            userDO.setTicket(ticket);
            return userDO;
        }
        return null;
    }

    public void addUser(UserDO user) {
        user.setUserId(UUID.randomUUID().toString());
        String salt = UUID.randomUUID().toString();
        user.setSalt(salt);
        Date now = new Date();
        user.setCreateTime(now);
        user.setIsLock(0);
        String rawPwd = user.getRawPwd();
        rawPwd = DecodeUtils.decode(rawPwd, "konglk");
        rawPwd = rawPwd + salt;
        rawPwd = DigestUtils.md5DigestAsHex(rawPwd.getBytes());
        user.setRawPwd(rawPwd);
        this.mongoTemplate.insert(user);
        this.logger.info("add new user {}", user.getUsername());
    }

    public UserDO addFriend(String userId, String destId, String remark) {
        Query query = new Query().addCriteria(Criteria.where("userId").is(userId));
        UserDO friend = (UserDO) this.mongoTemplate.findOne(new Query()
                .addCriteria(Criteria.where("userId").is(destId)), UserDO.class);
        if (friend == null) {
            return null;
        }
        FriendDO f = new FriendDO();
        f.setUserId(friend.getUserId());
        f.setProfileUrl(friend.getProfileUrl());
        f.setGender(f.getGender());
        f.setCountry(friend.getCountry());
        f.setCity(friend.getCity());
        f.setRemark(StringUtils.isEmpty(remark) ? friend.getNickname() : remark);
        f.setUsername(friend.getNickname());
        f.setSignature(friend.getSignature());
        f.setCreateTime(new Date());
        f.setLastUpdateTime(new Date());
        Update update = new Update();
        update.push("friends", f);
        logger.info("add friend {}", f.getUsername());
        return this.mongoTemplate.findAndModify(query, update, UserDO.class);
    }
}
