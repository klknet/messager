package com.konglk.ims.controller;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.model.FileMeta;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by konglk on 2019/6/10.
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private MongoDbFactory mongoDbFactory;
    @Autowired
    private RedisCacheService cacheService;

    @GetMapping("/img")
    public void getImg(@RequestParam String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String etag = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        String lastModified = request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
        FileMeta fileMeta = cacheService.getModifiedTime(id);
        String md5, time;
        GridFSFile gridFSFile = null;
        if (fileMeta != null) {
            md5 = fileMeta.getMd5();
            time = fileMeta.getUploadDate();
        }else {
            gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(id)));
            md5 = gridFSFile.getMD5();
            time = gridFSFile.getUploadDate().toString();
        }
        // 图片没有变化，返回304
        if (md5.equals(etag) && time.equals(lastModified)) {
            response.setHeader(HttpHeaders.CONTENT_TYPE, fileMeta.getContentType());
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileMeta.getContentLength()));
            response.setStatus(HttpStatus.NOT_MODIFIED.value());
            return;
        }

        if(gridFSFile == null)
            gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(id)));
        Document metadata = gridFSFile.getMetadata();
        response.setContentType(metadata.getString("_contentType"));
        response.setHeader(HttpHeaders.ETAG, gridFSFile.getMD5());
        response.setHeader(HttpHeaders.LAST_MODIFIED, gridFSFile.getUploadDate().toString());
        response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(gridFSFile.getLength()));
        ServletOutputStream outputStream = response.getOutputStream();
        GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDbFactory.getDb());
        gridFSBucket.downloadToStream(gridFSFile.getObjectId(), outputStream);
        FileMeta fm = new FileMeta(metadata.getString("_contentType"), gridFSFile.getLength(), gridFSFile.getMD5(), gridFSFile.getUploadDate().toString());
        cacheService.setModifiedTime(id, JSON.toJSONString(fm));
        outputStream.close();

    }
}
