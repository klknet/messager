package com.konglk.ims.service;

import com.konglk.ims.domain.FriendDO;
import com.konglk.ims.domain.UserDO;
import com.konglk.ims.event.ResponseEvent;
import com.konglk.ims.event.TopicProducer;
import com.konglk.ims.repo.IFriendRepository;
import com.konglk.ims.repo.IUserRepository;
import com.konglk.ims.util.EncryptUtil;
import com.konglk.ims.ws.PresenceManager;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import com.konglk.model.UserPO;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private PresenceManager presenceManager;
    @Value("${host}")
    private String host;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private TopicProducer topicProducer;
    @Autowired
    private IFriendRepository friendRepository;
    @Autowired
    private IUserRepository userRepository;

    public UserDO login(String unique, String pwd) {
        String raw = EncryptUtil.decrypt(pwd);
        logger.info("user {} password is {}", unique, pwd);
        UserDO userDO = userRepository.findByUsername(unique);
        if(userDO == null)
            userDO = userRepository.findByCellphone(unique);
        if (userDO == null)
            userDO = userRepository.findByMailbox(unique);
        if (userDO == null) {
            return null;
        }
        raw = DigestUtils.md5DigestAsHex((raw + userDO.getSalt()).getBytes());
        if (StringUtils.equals(raw, userDO.getRawPwd())) {
            String ticket = presenceManager.getTicket(userDO.getUserId());
            if(StringUtils.isNotEmpty(ticket)) {
                topicProducer.sendNotifyMessage(new ResponseEvent(new Response(ResponseStatus.U_KICK_OUT, Response.USER), userDO.getUserId()));
            }
            //登录凭证
            ticket = UUID.randomUUID().toString();
            presenceManager.addTicket(userDO.getUserId(), ticket);
            userDO.setTicket(ticket);
            eraseSensitive(userDO);
            List<FriendDO> friends = friendRepository.findByUserId(userDO.getUserId());
            userDO.setFriends(friends);
            return userDO;
        }
        return null;
    }


    @Transactional
    public void addUser(UserDO user) {
        populateData(user);
        userRepository.save(user);
        this.logger.info("add new user {}", user.getUsername());
    }

    /**
     * 更新头像
     * @param userId
     * @param multipartFile
     * @return
     */
    @Transactional
    public String updateAvatar(String userId, MultipartFile multipartFile) throws IOException {
        UserDO userDO = userRepository.findByUserId(userId);
        if (userDO == null)
            throw new IllegalArgumentException();
        ObjectId objectId = gridFsTemplate.store(multipartFile.getInputStream(), multipartFile.getOriginalFilename(), multipartFile.getContentType());
        //更新自己、好友、会话头像
        userRepository.updateAvatar(userId, objectId.toString());
        friendRepository.updateAvatar(userId, objectId.toString());
        conversationService.updateConvProfile(userId, objectId.toString());
        return objectId.toString();
    }

    /*
    批量添加用户
     */
    @Transactional
    public void batchInsert(List<UserDO> users) {
        if (CollectionUtils.isEmpty(users))
            return;
        int n = users.size();
        users.forEach(user -> populateData(user));
        if (n<=1024){
            userRepository.saveAll(users);
        }else {
            int page = n % 1024 == 0 ? n>>10 : (n>>10) +1;
            for (int i=0; i<page; i++) {
                userRepository.saveAll(users.subList(i<<10, Math.min(n, (i<<10) + 1024)));
            }

        }
    }

    /*
    修改备注
     */
    @Transactional
    public void setFriendNotename(String userId, String destId, String notename) {
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(destId) || StringUtils.isEmpty(notename))
            return;
        friendRepository.updateRemark(userId, destId, notename);
        conversationService.updateConversationName(userId, destId, notename);
    }

    /*
    添加朋友
     */
    @Transactional
    public void addFriend(String userId, String destId, String remark) {
        if (StringUtils.equals(userId, destId)) {
            throw new IllegalArgumentException("can't add yourself");
        }
        if(isFriend(userId, destId)) {
            logger.warn("{} and {} already friends", userId, destId);
            return;
        }
        UserDO friend = userRepository.findByUserId(destId);
        if (friend == null) {
            return;
        }
        FriendDO f = setFriendInfo(remark, userId, friend);
        friendRepository.save(f);
    }

    /*
    删除朋友
     */
    @Transactional
    public void delFriend(String userId, String friendId) {
        if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(friendId)) {
            return;
        }
        friendRepository.deleteByUserIdAndDestId(userId, friendId);
        logger.info("{} delete friend {}", userId, friendId);
    }

    /*
    添加多个朋友
     */
    @Transactional
    public void batchAddFriend(String userId, List<UserDO> friends) {
        List<FriendDO> friendDOS = friends.stream().map(userDO -> setFriendInfo(null, userId, userDO)).collect(Collectors.toList());
        friendRepository.saveAll(friendDOS);
    }

    /*
    查询所有userIds
     */
    public List<UserDO> findUsers(String[] userIds) {
        return userRepository.findByUserIdIn(userIds);
    }

    /*
    是否是好友
     */
    public boolean isFriend(String userId, String destId) {
        return friendRepository.existsByUserIdAndDestId(userId, destId);
    }

    public UserDO findByUserId(String userId) {
        UserDO userDO = userRepository.findByUserId(userId);
        String ticket = presenceManager.getTicket(userDO.getUserId());
        userDO.setTicket(ticket);
        userDO.setFriends(friendRepository.findByUserId(userId));
        return userDO;
    }

    /*
    分页获取用户
     */
    public List<UserDO> findUserByPage(int page, int size, Date time) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "userId"));
        return userRepository.findByCreateTimeAfter(time, pageable);
    }

    /*
    模糊查询用户信息
     */
    public List<UserPO> findUser(String username) {
        List<UserDO> userDOS = userRepository.findByUsernameLike(username+"%");
        return userDOS.stream().map(userDO -> {
            UserPO userPO = new UserPO();
            BeanUtils.copyProperties(userDO, userPO);
            return userPO;
        }).collect(Collectors.toList());
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

    private FriendDO setFriendInfo(String remark, String userId, UserDO friend) {
        FriendDO f = new FriendDO();
        f.setDestId(friend.getUserId());
        f.setUserId(userId);
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
