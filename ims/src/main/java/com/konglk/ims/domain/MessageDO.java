package com.konglk.ims.domain;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection="c_message")
@CompoundIndex(def="{'conversation_id':1, 'create_time':-1}")
public class MessageDO
{
  @Field("message_id")
  @Indexed(unique=true, name="i_message_id")
  private String messageId;
  @Field("conversation_id")
  private String conversationId;
  @Field("create_time")
  private Date createTime;
  private String content;
  @Field("user_id")
  private String userId;
  @Field("dest_id")
  private String destId;
  @Field
  private Integer type; //0-文字 1-图片 2-表情 3-语音 4-视频 5-撤回 -1-删除
  @Field("chat_type")
  private Integer chatType; //0- 一对一聊天  1- 群聊
  @Field("delete_ids")
  private List<String> deleteIds; //删除这条消息的用户id

  public String getMessageId()
  {
    return this.messageId;
  }
  
  public void setMessageId(String messageId)
  {
    this.messageId = messageId;
  }
  
  public String getConversationId()
  {
    return this.conversationId;
  }
  
  public void setConversationId(String conversationId)
  {
    this.conversationId = conversationId;
  }
  
  public Date getCreateTime()
  {
    return this.createTime;
  }
  
  public void setCreateTime(Date createTime)
  {
    this.createTime = createTime;
  }
  
  public String getContent()
  {
    return this.content;
  }
  
  public void setContent(String content)
  {
    this.content = content;
  }
  
  public String getUserId()
  {
    return this.userId;
  }
  
  public void setUserId(String userId)
  {
    this.userId = userId;
  }
  
  public String getDestId()
  {
    return this.destId;
  }
  
  public void setDestId(String destId)
  {
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

  public List<String> getDeleteIds() {
    return deleteIds;
  }

  public void setDeleteIds(List<String> deleteIds) {
    this.deleteIds = deleteIds;
  }
}
