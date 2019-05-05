package com.konglk.ims.service;

import com.konglk.ims.domain.MessageDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by konglk on 2019/4/20.
 */
@Service
public class MessageService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<MessageDO> prevMessages(String cid, Date createtime, boolean include) {
        Query query = new Query();
        //是否包含当前时间点的消息
        if (include) {
            query.addCriteria(Criteria.where("conversationId").is(cid).and("createTime").gte(createtime));
        } else {
            query.addCriteria(Criteria.where("conversationId").is(cid).and("createTime").gt(createtime));
        }
        query.with(PageRequest.of(0, 32, Sort.by(Sort.Direction.DESC, "createTime")));
        List<MessageDO> messageDOS = mongoTemplate.find(query, MessageDO.class);
        Collections.reverse(messageDOS);
        return messageDOS;
    }

    public void insert(MessageDO messageDO) {
        messageDO.setMessageId(UUID.randomUUID().toString());
        if(messageDO.getCreateTime() == null)
            messageDO.setCreateTime(new Date());
        mongoTemplate.insert(messageDO);
    }


}
