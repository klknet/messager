package com.konglk.model;

/**
 * Created by konglk on 2019/3/27.
 */
public class Request {
    private int type; //0-ping 1-login
    private String data;
    private String ticket; //凭证

    public Request() {
    }

    public Request(int type, String data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}
