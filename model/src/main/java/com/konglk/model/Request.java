package com.konglk.model;

/**
 * Created by konglk on 2019/3/27.
 */
public class Request {
    private int type;
    private String data;

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
}
