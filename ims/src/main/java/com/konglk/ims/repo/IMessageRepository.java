package com.konglk.ims.repo;

import com.konglk.ims.domain.MessageDO;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by konglk on 2019/8/14.
 */
public interface IMessageRepository extends MongoRepository<MessageDO, ObjectId> {

    boolean existsByMessageId(String messageId);
    MessageDO findByMessageIdAndUserId(String messageId, String userId);
    MessageDO findByMessageId(String messageId);
}
