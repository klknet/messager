package com.konglk.ims.repo;

import com.konglk.ims.domain.GroupChatDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Created by konglk on 2019/9/11.
 */
public interface IGroupChatRepository extends JpaRepository<GroupChatDO, String> {

    List<GroupChatDO> findByGroupIdIn(List<String> ids);
    List<GroupChatDO> findByUserId(String userId);

    List<GroupChatDO> findByGroupId(String groupId);
}
