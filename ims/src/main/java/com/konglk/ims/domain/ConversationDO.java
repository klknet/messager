package com.konglk.ims.domain;

import java.util.Date;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "c_conversation")
public class ConversationDO {
    @Indexed(name = "i_conversation_id")
    @Field("conversation_id")
    private String conversationId;
    @Indexed(name = "i_user_id")
    @Field("user_id")
    private String userId;
    @Indexed(name = "i_dest_id")
    @Field("dest_id")
    private String destId;
    @Field("create_time")
    private Date createTime;
    @Field("update_time")
    private Date updateTime;
    private String notename;
    @Field("last_msg")
    private String lastMsg;
    @Field("profile_url")
    private String profileUrl;
    @Field
    private int type;

    public String getConversationId() {
        return this.conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDestId() {
        return this.destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getNotename() {
        return this.notename;
    }

    public void setNotename(String notename) {
        this.notename = notename;
    }

    public String getLastMsg() {
        return this.lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getProfileUrl() {
        return this.profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
