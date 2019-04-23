package com.konglk.model;

import java.lang.reflect.Member;

/**
 * Created by konglk on 2019/4/22.
 */
public enum  ErrorStatus {

    TICKET_ERROR(60001, "ticket error");

    int code;
    String message;

    ErrorStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
