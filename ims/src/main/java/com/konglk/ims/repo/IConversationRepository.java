package com.konglk.ims.repo;

import com.konglk.ims.domain.ConversationDO;
import org.bson.types.ObjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by konglk on 2019/8/14.
 */
public interface IConversationRepository extends JpaRepository<ConversationDO, String> {

    boolean existsByUserIdAndDestId(String userId, String destId);

    ConversationDO findByUserIdAndDestId(String userId, String destId);

    ConversationDO findByConversationIdAndUserId(String conversationId, String userId);

    List<ConversationDO> findByUserIdOrderByUpdateTimeDesc(String userId);

    @Modifying
    @Query("update ConversationDO cd set cd.notename=:name where userId=:userId and destId=:destId")
    void updateConversationName(@Param("userId") String userId, @Param("destId") String destId, @Param("name") String name);

    @Modifying
    @Query("update ConversationDO cd set dnd=:dnd where conversationId=:conversationId and userId=:userId")
    void dndConversation(@Param("conversationId") String conversationId, @Param("userId") String userId, @Param("dnd")boolean dnd);

    @Modifying
    @Query("update ConversationDO cd set top=:top, topUpdateTime=:ts where conversationId=:conversationId and userId=:userId")
    void topConversation(@Param("conversationId") String conversationId, @Param("userId") String userId, @Param("top")boolean top, @Param("ts") Date ts);

    void removeByConversationIdAndUserId(String conversationId, String userId);

    @Modifying
    @Query("update ConversationDO cd set profileUrl=:url where destId=:userId")
    void updateAvatar(String userId, String url);

    @Modifying
    @Query("update ConversationDO cd set updateTime=:updateTime,messageType=:messageType,lastMsg=:lastMsg where destId=:destId")
    void updateConversationInfo(@Param("destId")String destId, @Param("updateTime")Date updateTime, @Param("messageType")int messageType, @Param("lastMsg")String lastMsg);

    @Transactional
    @Modifying
    @Query("update ConversationDO cd set notename=:notename where conversationId=:cid")
    void updateNotename(@Param("cid") String cid, @Param("notename") String notename);
}
