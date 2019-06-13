package com.konglk.ims.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

/**
 * Created by konglk on 2019/6/3.
 */
@Document(collection = "c_group_chat")
public class GroupChatDO {

    @Id
    private String id;

    private List<Member> members;

    public static class Member {
        @Field("user_id")
        private String userId;
        @Field("profile_url")
        private String profileUrl;
        private String nickname;

        public Member() {
        }

        public Member(String userId, String profileUrl, String nickname) {
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
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }


}
