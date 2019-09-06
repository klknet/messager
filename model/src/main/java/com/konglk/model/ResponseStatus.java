package com.konglk.model;

/**
 * Created by konglk on 2019/4/22.
 */
public enum ResponseStatus {

    TICKET_ERROR(60001, "ticket error"), //凭证错误
    U_KICK_OUT(80001, "kick out"), //踢出
    U_AGREE_FRIEND_REQUEST(80002, "agree friend request"),  //同意好友请求
    U_FRIEND_REQUEST(80003, "friend request"), //好友请求
    U_GROUP_CHAT(80004, "build group chat"),  //群聊
    U_UPDATE_NOTENAME(80005, "update friend notename"), //更新昵称

    M_TRANSFER_MESSAGE(70001, "message transfer"), //转发消息
    M_REVOCATION(70002, "message revocation"), //撤回消息
    M_DELETE_MESSAGE(70003, "message delete"), //删除消息
    M_UPDATE_CONVERSATION(70004, "update conversation content"),//更新会话显示最后一条消息
    M_ACK(70005, "ack"),//ack
    OK(200, "OK");

    int code;
    String message;

    ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }
}
