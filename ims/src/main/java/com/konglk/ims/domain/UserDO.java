package com.konglk.ims.domain;


import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Table(name = "im_user")
@Entity
public class UserDO {
    @Id
    @Column(name = "user_id")
    private String userId;
    @Column
    private String username;
    @Column
    private String nickname;
    @Column(name = "profile_url")
    private String profileUrl;
    @Column(name = "raw_pwd")
    private String rawPwd;
    @Column
    private String salt;
    @Column
    private Integer gender;
    @Column
    private Integer age;
    @Column
    private String country;
    @Column
    private String city;
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "last_login_time")
    private Date lastLoginTime;
    @Column(name = "is_lock")
    private Integer isLock;
    @Column
    private String cellphone;
    @Column
    private String mailbox;
    @Column
    private String signature;
    @Transient
    private List<FriendDO> friends;
    @Transient
    private String ticket;

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfileUrl() {
        return this.profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getRawPwd() {
        return this.rawPwd;
    }

    public void setRawPwd(String rawPwd) {
        this.rawPwd = rawPwd;
    }

    public String getSalt() {
        return this.salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Integer getGender() {
        return this.gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastLoginTime() {
        return this.lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Integer getIsLock() {
        return this.isLock;
    }

    public void setIsLock(Integer isLock) {
        this.isLock = isLock;
    }

    public String getCellphone() {
        return this.cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getMailbox() {
        return this.mailbox;
    }

    public void setMailbox(String mailbox) {
        this.mailbox = mailbox;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<FriendDO> getFriends() {
        return this.friends;
    }

    public void setFriends(List<FriendDO> friends) {
        this.friends = friends;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}
