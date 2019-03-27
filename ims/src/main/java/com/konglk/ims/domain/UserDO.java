package com.konglk.ims.domain;

import java.util.Date;
import java.util.List;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection="c_user")
public class UserDO
{
  @Field("user_id")
  @Indexed(name="i_user_id", unique=true)
  private String userId;
  @Indexed(name="i_user_username", unique=true)
  private String username;
  private String nickname;
  @Field("profile_url")
  private String profileUrl;
  @Field("raw_pwd")
  private String rawPwd;
  private String salt;
  private Integer gender;
  private Integer age;
  private String country;
  private String city;
  @Field("create_time")
  private Date createTime;
  @Field("last_login_time")
  private Date lastLoginTime;
  @Field("is_lock")
  private Integer isLock;
  @Field
  @Indexed(name="i_user_cellphone", unique=true)
  private String cellphone;
  @Field
  @Indexed(name="i_user_mailbox", unique=true)
  private String mailbox;
  private String signature;
  private List<FriendDO> friends;
  @Field("black_list")
  private List<FriendDO> blackList;
  
  public String getUserId()
  {
    return this.userId;
  }
  
  public void setUserId(String userId)
  {
    this.userId = userId;
  }
  
  public String getUsername()
  {
    return this.username;
  }
  
  public void setUsername(String username)
  {
    this.username = username;
  }
  
  public String getNickname()
  {
    return this.nickname;
  }
  
  public void setNickname(String nickname)
  {
    this.nickname = nickname;
  }
  
  public String getProfileUrl()
  {
    return this.profileUrl;
  }
  
  public void setProfileUrl(String profileUrl)
  {
    this.profileUrl = profileUrl;
  }
  
  public String getRawPwd()
  {
    return this.rawPwd;
  }
  
  public void setRawPwd(String rawPwd)
  {
    this.rawPwd = rawPwd;
  }
  
  public String getSalt()
  {
    return this.salt;
  }
  
  public void setSalt(String salt)
  {
    this.salt = salt;
  }
  
  public Integer getGender()
  {
    return this.gender;
  }
  
  public void setGender(Integer gender)
  {
    this.gender = gender;
  }
  
  public Integer getAge()
  {
    return this.age;
  }
  
  public void setAge(Integer age)
  {
    this.age = age;
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
  
  public Date getLastLoginTime()
  {
    return this.lastLoginTime;
  }
  
  public void setLastLoginTime(Date lastLoginTime)
  {
    this.lastLoginTime = lastLoginTime;
  }
  
  public Integer getIsLock()
  {
    return this.isLock;
  }
  
  public void setIsLock(Integer isLock)
  {
    this.isLock = isLock;
  }
  
  public String getCellphone()
  {
    return this.cellphone;
  }
  
  public void setCellphone(String cellphone)
  {
    this.cellphone = cellphone;
  }
  
  public String getMailbox()
  {
    return this.mailbox;
  }
  
  public void setMailbox(String mailbox)
  {
    this.mailbox = mailbox;
  }
  
  public String getSignature()
  {
    return this.signature;
  }
  
  public void setSignature(String signature)
  {
    this.signature = signature;
  }
  
  public List<FriendDO> getFriends()
  {
    return this.friends;
  }
  
  public void setFriends(List<FriendDO> friends)
  {
    this.friends = friends;
  }
  
  public List<FriendDO> getBlackList()
  {
    return this.blackList;
  }
  
  public void setBlackList(List<FriendDO> blackList)
  {
    this.blackList = blackList;
  }
}
