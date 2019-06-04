package com.konglk.ims.domain;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

/**
 * Created by konglk on 2019/6/3.
 */
@Document(collection = "c_group_chat")
public class GroupChatDO {

    @Indexed
    @Field("group_id")
    private String groupId;

    @Field("user_ids")
    private List<String> userIds;

    @Field("create_time")
    private Date createTime;

    @Field("profile_url")
    private String profileUrl;


}
