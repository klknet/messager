package com.konglk.ims.event;

import com.konglk.model.Response;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Created by konglk on 2019/6/12.
 */
public class ResponseEvent extends ApplicationEvent {

    private String userId;
    private List<String> userIds;
    private Response source;

    public ResponseEvent(Response source, String userId) {
        super(source);
        this.source = source;
        this.userId = userId;
    }

    public ResponseEvent(Response source, List<String> userIds) {
        super(source);
        this.source = source;
        this.userIds = userIds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public Object getSource() {
        return source;
    }

    public void setSource(Response source) {
        this.source = source;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
