package com.konglk.ims.repo;

import com.konglk.ims.domain.MessageDO;
import org.bson.types.ObjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by konglk on 2019/8/14.
 */
public interface IMessageRepository extends JpaRepository<MessageDO, String> {

    boolean existsByMessageId(String messageId);
    MessageDO findByMessageIdAndUserId(String messageId, String userId);
    MessageDO findByMessageId(String messageId);
    @Transactional
    @Modifying
    @Query("update MessageDO md set type=:type where messageId=:messageId and userId=:userId")
    void updateMsgType(@Param("messageId") String messageId, @Param("userId") String userId, @Param("type") int type);

    @Transactional
    @Modifying
    @Query("update MessageDO md set delete_ids=:deleteIds where message_id=:messageId and user_id=:userId")
    void updateDeleteIds(@Param("messageId") String messageId, @Param("userId") String userId, @Param("deleteIds") String deleteIds);

    MessageDO findFirstByConversationIdAndCreateTimeBefore(String cid, Date time);

    /**
     * 当前消息是否是最后一条消息
     * @param cid
     * @param uid
     * @param time
     * @return
     */
    @Query(nativeQuery = true, value="select 1 from im_message where conversation_id=:cid " +
            "and (delete_ids is null or delete_ids not like:uid) and type>=0 and type != 5 and create_time>=:time")
    long isLastMsg(@Param("cid") String cid, @Param("uid") String uid, @Param("time") Date time);

    /**
     * 上一条正常的消息
     * @param cid
     * @param uid
     * @param time
     * @return
     */
    @Query(nativeQuery = true, value="select * from im_message where conversation_id=:cid " +
            "and (delete_ids is null or delete_ids not like:uid) " +
            "and type>=0 and type != 5 and create_time<:time order by create_time desc limit 1")
    MessageDO prevMessage(@Param("cid") String cid, @Param("uid") String uid, @Param("time") Date time);

}
