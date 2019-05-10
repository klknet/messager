package com.konglk.ims.domain;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * Created by konglk on 2019/5/10.
 */
@Document(collection = "c_failed_msg")
public class FailedMessageDO {

    private String text;
    private Date ts;
    @Field("message_id")
    @Indexed(name = "i_message_id")
    private String messageId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
