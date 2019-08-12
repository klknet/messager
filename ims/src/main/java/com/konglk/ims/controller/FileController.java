package com.konglk.ims.controller;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.model.FileMeta;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

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

    @PostMapping("/upload")
    public void uploadFile(@RequestParam String id, @RequestParam String fileName, HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String rangeHeader = request.getHeader(HttpHeaders.RANGE);
            ServletInputStream inputStream = request.getInputStream();
            //小文件直接上传
            if (StringUtils.isEmpty(rangeHeader)) {
                Part filePart = request.getPart("file");
                ObjectId objectId = gridFsTemplate.store(inputStream, fileName, filePart.getContentType());
                response.setStatus(HttpStatus.OK.value());
                response.getOutputStream().print(objectId.toString());
                return;
            }
            //大文件支持断点上传
            File file = new File(request.getServletContext().getRealPath("/")+id);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "w");
            randomAccessFile.seek(randomAccessFile.length());

            byte[] buff = new byte[1024];
            while (!inputStream.isFinished()) {
                int actual = inputStream.read(buff);
                randomAccessFile.write(buff, 0, actual);
            }
            response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes="+randomAccessFile.length());
            randomAccessFile.close();


        } catch (InvalidPathException e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
    }

    @PutMapping("/uploadDone")
    public void uploadDone(@RequestParam String id, String msgId) {

    }
}
