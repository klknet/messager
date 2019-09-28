package com.konglk.ims.repo;

import com.konglk.ims.domain.FriendDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by konglk on 2019/9/11.
 */
public interface IFriendRepository extends JpaRepository<FriendDO, Long> {

    boolean existsByUserIdAndDestId(String userId, String destId);

    void deleteByUserIdAndDestId(String userId, String destId);

    List<FriendDO> findByUserId(String userId);

    @Modifying
    @Query("update FriendDO fd set fd.remark=:remark where fd.userId=:userId and fd.destId=:destId")
    void updateRemark(@Param("userId")String userId, @Param("destId")String destId, @Param("remark")String remark);

    @Modifying
    @Query("update FriendDO fd set profileUrl=:url where destId=:userId")
    void updateAvatar(@Param("userId")String userId, @Param("url")String url);

}
