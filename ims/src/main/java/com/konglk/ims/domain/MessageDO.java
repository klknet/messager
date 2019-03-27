package com.konglk.ims.domain;

import java.util.Date;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection="c_message")
public class MessageDO
{
  @Field("meessage_id")
  @Indexed(unique=true, name="i_message_id")
  private String messageId;
  @Field("conversation_id")
  private String conversationId;
  @Field("create_time")
  private Date createTime;
  private String content;
  @Indexed(name="i_user_id")
  @Field("user_id")
  private String userId;
  @Indexed(name="i_dest_id")
  @Field("dest_id")
  private String destId;
  private int type;
  
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
}
