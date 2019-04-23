package com.konglk.ims.cache;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by konglk on 2019/4/22.
 */
@Service
public class RedisCacheService {

    @Autowired
    private RedisTemplate redisTemplate;

    public void setUserTicket(String userId, String ticket) {
        // 7天免登陆
        redisTemplate.opsForValue().set(Constants.USER_TICKET+":"+userId, ticket, 7, TimeUnit.DAYS);
    }

    public String getUserTicket(String userId) {
        Object ticket = redisTemplate.opsForValue().get(Constants.USER_TICKET + ":" + userId);
        return ticket == null ? null : ticket.toString();
    }
}
