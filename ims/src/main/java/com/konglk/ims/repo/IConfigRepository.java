package com.konglk.ims.repo;

import com.konglk.ims.domain.ConfigDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by konglk on 2019/9/11.
 */
public interface IConfigRepository extends JpaRepository<ConfigDO, Long> {

    ConfigDO findByName(String name);
    @Modifying
    @Query("update ConfigDO cd set cd.value=:value where cd.name=:name")
    void updateConfigValue(@Param("name") String name, @Param("value") String value);
}
