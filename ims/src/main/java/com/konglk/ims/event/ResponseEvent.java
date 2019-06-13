package com.konglk.ims.event;

import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Created by konglk on 2019/6/12.
 */
public class ResponseEvent extends ApplicationEvent {

    private String userId;

    public ResponseEvent(Object source, String userId) {
        super(source);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
