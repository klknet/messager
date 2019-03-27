package com.konglk.ims.domain;

import java.util.Date;
import org.springframework.data.mongodb.core.mapping.Field;

public class FriendDO
{
  @Field("user_id")
  private String userId;
  @Field("profile_url")
  private String profileUrl;
  private Integer gender;
  private String signature;
  private String remark;
  private String username;
  private String country;
  private String city;
  @Field("create_time")
  private Date createTime;
  @Field("last_update_time")
  private Date lastUpdateTime;
  
  public String getUserId()
  {
    return this.userId;
  }
  
  public void setUserId(String userId)
  {
    this.userId = userId;
  }
  
  public String getProfileUrl()
  {
    return this.profileUrl;
  }
  
  public void setProfileUrl(String profileUrl)
  {
    this.profileUrl = profileUrl;
  }
  
  public Integer getGender()
  {
    return this.gender;
  }
  
  public void setGender(Integer gender)
  {
    this.gender = gender;
  }
  
  public String getSignature()
  {
    return this.signature;
  }
  
  public void setSignature(String signature)
  {
    this.signature = signature;
  }
  
  public String getRemark()
  {
    return this.remark;
  }
  
  public void setRemark(String remark)
  {
    this.remark = remark;
  }
  
  public String getCountry()
  {
    return this.country;
  }
  
  public void setCountry(String country)
  {
    this.country = country;
  }
  
  public String getCity()
  {
    return this.city;
  }
  
  public void setCity(String city)
  {
    this.city = city;
  }
  
  public Date getCreateTime()
  {
    return this.createTime;
  }
  
  public void setCreateTime(Date createTime)
  {
    this.createTime = createTime;
  }
  
  public Date getLastUpdateTime()
  {
    return this.lastUpdateTime;
  }
  
  public void setLastUpdateTime(Date lastUpdateTime)
  {
    this.lastUpdateTime = lastUpdateTime;
  }
  
  public String getUsername()
  {
    return this.username;
  }
  
  public void setUsername(String username)
  {
    this.username = username;
  }
}
