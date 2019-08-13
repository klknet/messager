package com.konglk.ims.model;

/**
 * Created by konglk on 2019/8/13.
 */
public class FileDetail {
    private Long size;
    private String filename;
    private String contentType;

    public FileDetail() {
    }

    public FileDetail(Long size, String filename, String contentType) {
        this.size = size;
        this.filename = filename;
        this.contentType = contentType;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
