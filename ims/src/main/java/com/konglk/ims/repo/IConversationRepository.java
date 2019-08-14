package com.konglk.ims.repo;

import com.konglk.ims.domain.ConversationDO;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by konglk on 2019/8/14.
 */
public interface IConversationRepository extends MongoRepository<ConversationDO, ObjectId> {
}
