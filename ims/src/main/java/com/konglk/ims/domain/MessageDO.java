package com.konglk.ims.domain;

import java.util.Date;

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
  private int type; //0-文字 1-图片 2-表情 3-语音 4-视频 5-撤回 -1-删除
  @Field("chat_type")
  private int chatType; //0- 一对一聊天  1- 群聊

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
  
  public int getType()
  {
    return this.type;
  }
  
  public void setType(int type)
  {
    this.type = type;
  }

  public int getChatType() {
    return chatType;
  }

  public void setChatType(int chatType) {
    this.chatType = chatType;
  }
}
