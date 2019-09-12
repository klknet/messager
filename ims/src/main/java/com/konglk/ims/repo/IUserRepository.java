package com.konglk.ims.repo;

import com.konglk.ims.domain.UserDO;
import org.apache.catalina.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by konglk on 2019/8/14.
 */
public interface IUserRepository extends JpaRepository<UserDO, String> {

    List<UserDO> findByUserIdIn(String[] userIds);

    UserDO findByUserId(String userId);

    //查询指定时间后创建的用户
    List<UserDO> findByCreateTimeAfter(Date ts, Pageable pageable);

    List<UserDO> findByUsernameLike(String username);

    UserDO findByUsername(String username);

    UserDO findByCellphone(String cellphone);

    UserDO findByMailbox(String mailbox);

    @Modifying
    @Query("update UserDO ud set ud.profileUrl=:url where userId=:userId")
    void updateAvatar(@Param("userId") String userId, @Param("url") String url);
}
