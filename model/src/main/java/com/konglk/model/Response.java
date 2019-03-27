package com.konglk.model;

/**
 * Created by konglk on 2019/3/27.
 */
public class Response {
    private int code;
    private String message;
    private String data;
    private int type;

    public Response() {
    }

    public Response(int code, String message, String data, int type) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
