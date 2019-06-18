package com.konglk.ims.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
        if (delta<=0)
            return;
        redisTemplate.opsForHash().increment(Constants.CONV_NUMBER+":"+userId, id, delta);
    }

    public void incUnreadNum(List<String> userIds, String id, long delta) {
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (String userId: userIds) {
                    connection.hIncrBy((Constants.CONV_NUMBER+":"+userId).getBytes(), id.getBytes(), delta);
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
}
