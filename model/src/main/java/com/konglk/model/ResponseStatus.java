package com.konglk.model;

/**
 * Created by konglk on 2019/4/22.
 */
public enum ResponseStatus {

    TICKET_ERROR(60001, "ticket error"),
    KICK_OUT(80001, "kick out"),
    OK(200, "OK");

    int code;
    String message;

    ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
