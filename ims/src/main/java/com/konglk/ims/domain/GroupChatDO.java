package com.konglk.ims.domain;

import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.*;

/**
 * Created by konglk on 2019/6/3.
 */
@Table(name = "im_group_chat")
@Entity
public class GroupChatDO {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(name = "group_id")
    private String groupId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "profile_url")
    private String profileUrl;
    private String nickname;

    public GroupChatDO() {
    }

    public GroupChatDO(String id, String userId, String profileUrl, String nickname) {
        this.groupId = id;
        this.userId = userId;
        this.profileUrl = profileUrl;
        this.nickname = nickname;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
