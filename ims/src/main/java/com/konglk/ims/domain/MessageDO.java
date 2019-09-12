package com.konglk.ims.domain;

import com.konglk.ims.model.FileDetail;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Table(name = "im_message")
@Entity
public class MessageDO {
    @Id
    @Column(name = "message_id")
    private String messageId;
    @Column(name = "conversation_id")
    private String conversationId;
    @Column(name = "create_time")
    private Date createTime;
    private String content;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "dest_id")
    private String destId;
    @Column
    private Integer type; //0-文字 1-图片 2-文件 3-语音 4-视频 5-撤回 -1-删除
    @Column(name = "chat_type")
    private Integer chatType; //0- 一对一聊天  1- 群聊
    @Column(name = "delete_ids")
    private String deleteIds; //删除这条消息的用户id

    @Transient
    private FileDetail fileDetail;

    public String getMessageId() {
        return this.messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getConversationId() {
        return this.conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getChatType() {
        return chatType;
    }

    public void setChatType(Integer chatType) {
        this.chatType = chatType;
    }

    public String getDeleteIds() {
        return deleteIds;
    }

    public void setDeleteIds(String deleteIds) {
        this.deleteIds = deleteIds;
    }

    public FileDetail getFileDetail() {
        return fileDetail;
    }

    public void setFileDetail(FileDetail fileDetail) {
        this.fileDetail = fileDetail;
    }
}
