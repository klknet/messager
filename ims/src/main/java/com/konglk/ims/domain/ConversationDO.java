package com.konglk.ims.domain;

import java.util.Date;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "c_conversation")
public class ConversationDO {
    @Id
    private String id;
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
    private Boolean top;//置顶标识
    @Field
    private Date topUpdateTime;
    @Field
    private Boolean dnd; //Don't disturb
    @Field("hide_name")
    private boolean hideName;// 群聊时隐藏昵称
    @Field
    private Integer type; //0: 一对一 1: 群聊
    @Field("message_type")
    private Integer messageType; //消息类型
    @Transient
    private long unreadCount; //未读消息

    @Transient
    private GroupChatDO groupChat;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Boolean getTop() {
        return top;
    }

    public void setTop(Boolean top) {
        this.top = top;
    }

    public Boolean getDnd() {
        return dnd;
    }

    public void setDnd(Boolean dnd) {
        this.dnd = dnd;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTopUpdateTime() {
        return topUpdateTime;
    }

    public void setTopUpdateTime(Date topUpdateTime) {
        this.topUpdateTime = topUpdateTime;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public GroupChatDO getGroupChat() {
        return groupChat;
    }

    public void setGroupChat(GroupChatDO groupChat) {
        this.groupChat = groupChat;
    }

    public boolean isHideName() {
        return hideName;
    }

    public void setHideName(boolean hideName) {
        this.hideName = hideName;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversationDO that = (ConversationDO) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
