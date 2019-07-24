package com.konglk.ims.service;

import com.konglk.ims.domain.FriendDO;
import com.konglk.ims.domain.UserDO;
import com.konglk.ims.util.DecodeUtils;

import java.util.*;
import java.util.stream.Collectors;

import com.konglk.ims.util.EncryptUtil;
import com.konglk.ims.util.NameRandomUtil;
import com.konglk.ims.ws.ChatEndPoint;
import com.konglk.ims.ws.ConnectionHolder;
import com.konglk.model.UserPO;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

@Service
public class UserService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SALT = "konglk";
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ConnectionHolder connectionHolder;
    @Value("${host}")
    private String host;
    @Autowired
    private ReplyService replyService;


    public UserDO login(String unique, String pwd) {
        String raw = EncryptUtil.decrypt(pwd);
        logger.info("user {} password is {}", unique, pwd);
        Query query = new Query();
        query.addCriteria(new Criteria().orOperator(Criteria.where("username").is(unique),
                Criteria.where("cellphone").is(unique),
                Criteria.where("mailbox").is(unique)));
        UserDO userDO = mongoTemplate.findOne(query, UserDO.class);
        if (userDO == null) {
            return null;
        }
        raw = DigestUtils.md5DigestAsHex((raw + userDO.getSalt()).getBytes());
        if (StringUtils.equals(raw, userDO.getRawPwd())) {
            String ticket = connectionHolder.getTicket(userDO.getUserId());
            if(StringUtils.isNotEmpty(ticket)) {
                ChatEndPoint client = connectionHolder.getClient(userDO.getUserId());
                if (client != null) {
                    replyService.replyKickout(client);
                }
            }
            //登录凭证
            ticket = UUID.randomUUID().toString();
            connectionHolder.addTicket(userDO.getUserId(), ticket);
            userDO.setTicket(ticket);
            eraseSensitive(userDO);
            return userDO;
        }
        return null;
    }



    public void addUser(UserDO user) {
        populateData(user);
        this.mongoTemplate.insert(user);
        this.logger.info("add new user {}", user.getUsername());
    }

    /*
    批量添加用户
     */
    public void batchInsert(List<UserDO> users) {
        if (CollectionUtils.isEmpty(users))
            return;
        int n = users.size();
        users.forEach(user -> populateData(user));
        if (n<=1024){
            mongoTemplate.insertAll(users);
        }else {
            int page = n % 1024 == 0 ? n>>10 : (n>>10) +1;
            for (int i=0; i<page; i++) {
                mongoTemplate.insertAll(users.subList(i<<10, Math.min(n, (i<<10) + 1024)));
            }

        }
    }

    /*
    修改备注
     */
    public void setFriendNotename(String userId, String destId, String notename) {
        Query query = new Query();
        query.addCriteria(Criteria.where("user_id").is(userId).and("friends.user_id").is(destId));
        Update update = new Update();
        update.set("friends.$.remark", notename);
        mongoTemplate.updateFirst(query, update, UserDO.class);
    }

    /*
    添加朋友
     */
    public UserDO addFriend(String userId, String destId, String remark) {
        if (StringUtils.equals(userId, destId)) {
            throw new IllegalArgumentException("can't add yourself");
        }
        if(isFriend(userId, destId)) {
            logger.warn("{} and {} already friends", userId, destId);
            return null;
        }
        Query query = new Query().addCriteria(Criteria.where("userId").is(userId));
        UserDO friend = this.mongoTemplate.findOne(new Query()
                .addCriteria(Criteria.where("userId").is(destId)), UserDO.class);
        if (friend == null) {
            return null;
        }
        FriendDO f = setFriendInfo(remark, friend);
        Update update = new Update();
        update.addToSet("friends", f);
        logger.info("add friend {}", f.getUsername());
        return this.mongoTemplate.findAndModify(query, update, UserDO.class);
    }

    /*
    删除朋友
     */
    public void delFriend(String userId, String friendId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        Update update = new Update();
        update.pull("friends", new BasicDBObject("userId", friendId));
        mongoTemplate.updateFirst(query, update, UserDO.class);
        logger.info("{} delete friend {}", userId, friendId);
    }


    /*
    添加多个朋友
     */
    public void batchAddFriend(String userId, List<UserDO> friends) {
        Query query = new Query().addCriteria(Criteria.where("userId").is(userId));
        List<FriendDO> friendDOS = friends.stream().map(userDO -> {
            return setFriendInfo(null, userDO);
        }).collect(Collectors.toList());
        Update update = new Update();
        update.addToSet("friends", BasicDBObjectBuilder.start("$each", friendDOS).get());
        Random random = new Random();
        this.mongoTemplate.findAndModify(query, update, UserDO.class);
    }

    /*
    查询所有userIds
     */
    public List<UserDO> findUsers(String[] userIds) {
        return mongoTemplate.find(new Query(Criteria.where("userId").in(userIds)), UserDO.class);
    }

    /*
    是否是好友
     */
    public boolean isFriend(String userId, String destId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId).and("friends.userId").is(destId));
        return mongoTemplate.exists(query, UserDO.class);
    }

    public UserDO findByUserId(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        UserDO userDO = mongoTemplate.findOne(query, UserDO.class);
        userDO.setTicket(connectionHolder.getTicket(userId));
        return userDO;
    }

    /*
    分页获取用户
     */
    public List<UserDO> findUserByPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "userId"));
        Query query = new Query();
        query.with(pageable);
        List<UserDO> userDOS = mongoTemplate.find(query, UserDO.class);
        return userDOS;
    }

    /*
    模糊查询用户信息
     */
    public List<UserPO> findUser(String username) {
        Document queryObj = new Document("username", new Document("$regex", "^" + username + ".*$"));
        Document fieldObj = new Document();
        for(String field: UserPO.fields) {
            fieldObj.put(field, 1);
        }
        BasicQuery basicQuery = new BasicQuery(queryObj, fieldObj);
        List<UserDO> userDOs = mongoTemplate.find(basicQuery, UserDO.class);
        return userDOs.stream().map(userDO -> {
            UserPO userPO = new UserPO();
            BeanUtils.copyProperties(userDO, userPO);
            return userPO;
        }).collect(Collectors.toList());
    }

    /*
    随机获取n个用户
     */
    public List<UserDO> randomUser(int n) {
        TypedAggregation<UserDO> aggregation = Aggregation.newAggregation(UserDO.class, Aggregation.sample(n));
        return mongoTemplate.aggregate(aggregation, UserDO.class).getMappedResults();
    }

    private void populateData(UserDO user) {
        user.setUserId(UUID.randomUUID().toString());
        String salt = UUID.randomUUID().toString();
        user.setSalt(salt);
        Date now = new Date();
        user.setCreateTime(now);
        user.setIsLock(0);
        String rawPwd = user.getRawPwd();
        rawPwd = EncryptUtil.decrypt(rawPwd);
        rawPwd = rawPwd + salt;
        rawPwd = DigestUtils.md5DigestAsHex(rawPwd.getBytes());
        user.setRawPwd(rawPwd);
        if(StringUtils.isEmpty(user.getProfileUrl())) {
            user.setProfileUrl("http://39.106.133.40/static/"+
                    (user.getGender()==1 ? "default_male.jpg" : "default_female.jpg"));
        }
    }

    private FriendDO setFriendInfo(String remark, UserDO friend) {
        FriendDO f = new FriendDO();
        f.setUserId(friend.getUserId());
        f.setProfileUrl(friend.getProfileUrl());
        f.setGender(friend.getGender());
        f.setCountry(friend.getCountry());
        f.setCity(friend.getCity());
        f.setRemark(StringUtils.isEmpty(remark) ? friend.getNickname() : remark);
        f.setUsername(friend.getNickname());
        f.setSignature(friend.getSignature());
        f.setCreateTime(new Date());
        f.setLastUpdateTime(new Date());
        return f;
    }

    /*
    抹掉铭感信息
     */
    public void eraseSensitive(UserDO userDO) {
        userDO.setRawPwd(null);
        userDO.setSalt(null);
    }


}
