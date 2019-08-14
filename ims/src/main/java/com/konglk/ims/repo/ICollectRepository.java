package com.konglk.ims.repo;

import com.konglk.ims.domain.CollectDO;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by konglk on 2019/8/14.
 */
public interface ICollectRepository extends MongoRepository<CollectDO, ObjectId> {

    List<CollectDO> findByUserId(String userId);

    boolean existsByUserIdAndContent(String userId, String content);
}
