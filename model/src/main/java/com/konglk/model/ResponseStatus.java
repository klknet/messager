package com.konglk.model;

/**
 * Created by konglk on 2019/4/22.
 */
public enum ResponseStatus {

    TICKET_ERROR(60001, "ticket error"), //凭证错误
    KICK_OUT(80001, "kick out"), //踢出
    AGREE_FRIEND_REQUEST(80002, "agree friend request"),  //同意好友请求
    FRIEND_REQUEST(80003, "friend request"), //好友请求
    GROUP_CHAT(80004, "build group chat"),  //群聊
    UPDATE_NOTENAME(80005, "update friend notename"), //更新昵称
    TRANSFER_MESSAGE(70001, "message transfer"), //转发消息
    REVOCATION(70002, "message revocation"), //撤回消息
    OK(200, "OK");

    int code;
    String message;

    ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
