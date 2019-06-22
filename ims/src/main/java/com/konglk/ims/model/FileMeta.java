package com.konglk.ims.model;

/**
 * Created by konglk on 2019/6/22.
 */
public class FileMeta {
    private String contentType;
    private long contentLength;
    private String md5;
    private String uploadDate;

    public FileMeta() {
    }

    public FileMeta(String contentType, long contentLength, String md5, String uploadDate) {
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.md5 = md5;
        this.uploadDate = uploadDate;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }
}
