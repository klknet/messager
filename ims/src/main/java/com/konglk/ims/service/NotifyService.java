package com.konglk.ims.service;

import com.konglk.ims.domain.NotifyDO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by konglk on 2019/6/12.
 */
@Service
public class NotifyService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void saveNotify(String userId, String data) {
        if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(data)){
            return;
        }
        NotifyDO notifyDO = new NotifyDO();
        notifyDO.setCreatetime(new Date());
        notifyDO.setData(data);
        notifyDO.setUserId(userId);
        mongoTemplate.insert(notifyDO);
    }

}
