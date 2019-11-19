package com.konglk.ims.cache;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.domain.GroupChatDO;
import com.konglk.ims.model.FileMeta;
import com.konglk.model.Response;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.security.acl.Group;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by konglk on 2019/4/22.
 */
@Service
public class RedisCacheService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RedisTemplate redisTemplate;

    public void setUserTicket(String userId, String ticket) {
        // 7天免登陆
        redisTemplate.opsForValue().set(Constants.USER_TICKET+":"+userId, ticket, 7, TimeUnit.DAYS);
    }

    public String getUserTicket(String userId) {
        Object ticket = null;
        try {
            ticket = redisTemplate.opsForValue().get(Constants.USER_TICKET + ":" + userId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return ticket == null ? null : ticket.toString();
    }

    public void delUserTicket(String userId) {
        redisTemplate.delete(Constants.USER_TICKET+":"+userId);
    }

    /*
    未读消息+1
     */
    public void incUnreadNum(String userId, String id, long delta) {
        String key = Constants.CONV_NUMBER+":"+userId;
        if (delta<=0)
            return;
        redisTemplate.opsForHash().increment(key, id, delta);
        Long expire = redisTemplate.getExpire(key);
        if (expire == -1)
            redisTemplate.expire(key, 30, TimeUnit.DAYS);

    }

    /*
    批量未读消息
     */
    public void incUnreadNum(List<String> userIds, String id, long delta) {
        if (CollectionUtils.isEmpty(userIds))
            return;
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                for (String userId: userIds) {
                    byte[] key = (Constants.CONV_NUMBER+":"+userId).getBytes();
                    connection.hIncrBy(key, id.getBytes(), delta);
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_MONTH, 30);
                    connection.expireAt(key, calendar.getTimeInMillis());
                }
                return null;
            }
        });
    }

    /*
    获取用户会话未读消息
     */
   public Map<String,String> getUnreadNum(String userId) {
       Map entries = redisTemplate.opsForHash().entries(Constants.CONV_NUMBER + ":" + userId);
       return entries;
   }

    public void delUnreadNum(String userId, String id) {
        redisTemplate.opsForHash().delete(Constants.CONV_NUMBER+":"+userId, id);
    }

    /*
    图片最后修改时间
     */
    public void setModifiedTime(String id, String val) {
       redisTemplate.opsForValue().set(Constants.NOT_MODIFIED+":"+id, val, 1, TimeUnit.DAYS);
    }

    public FileMeta getModifiedTime(String id) {
        Object data = redisTemplate.opsForValue().get(Constants.NOT_MODIFIED + ":" + id);
        if (data == null)
            return null;
        return JSON.parseObject(data.toString(), FileMeta.class);
    }

    /**
     * 设置全局在线状态
     * @param userId
     */
    public void setOnline(String userId) {
        redisTemplate.opsForHash().put(Constants.USER_ONLINE, userId, String.valueOf(System.currentTimeMillis()));
    }

    /**
     * 离线
     * @param userId
     */
    public void setOffline(String userId) {
        redisTemplate.opsForHash().delete(Constants.USER_ONLINE, userId);
    }

    /**
     * 判断用户是否在线
     * @param userId
     * @return
     */
    public boolean isOnline(String userId) {
        Object o = redisTemplate.opsForHash().get(Constants.USER_ONLINE, userId);
        if (o == null)
            return false;
        return true;
    }

    /**
     * 批量判断用户是否在线
     */
    public List<Boolean> isOnline(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds))
            return null;
        return redisTemplate.executePipelined(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                for (String userId: userIds) {
                    byte[] key = (Constants.USER_ONLINE).getBytes();
                    connection.hExists(key, userId.getBytes());
                }
                return null;
            }
        });
    }

    public void setGroupChat(String id, List<GroupChatDO> groupChat) {
        String key = Constants.GOURP_CHAT+":"+id;
        redisTemplate.opsForValue().set(key, JSON.toJSONString(groupChat));
        redisTemplate.expire(key, 5L, TimeUnit.MINUTES);
    }

    public List<GroupChatDO> getGroupChat(String id) {
        String key = Constants.GOURP_CHAT+":"+id;
        Object v = redisTemplate.opsForValue().get(key);
        if (v==null) {
            return null;
        }
        return JSON.parseArray(v.toString(), GroupChatDO.class);
    }

}
