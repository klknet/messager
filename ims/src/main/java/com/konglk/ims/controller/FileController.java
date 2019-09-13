package com.konglk.ims.controller;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.model.FileDetail;
import com.konglk.ims.model.FileMeta;
import com.konglk.ims.event.TopicProducer;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.*;

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
    @Autowired
    private ThreadPoolTaskExecutor executor;
    @Autowired
    private TopicProducer producer;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 获取图片，支持浏览器304 缓存
     * @param id
     * @param request
     * @param response
     * @throws IOException
     */
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

    /**
     * 文件上传，支持断点续传
     * @param id
     * @param file
     * @param request
     * @param response
     * @throws Exception
     */
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadFile(@RequestParam String id, MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RandomAccessFile randomAccessFile = null;
        try {
            String rangeHeader = request.getHeader(HttpHeaders.RANGE);
            //大文件支持断点上传
            byte[] bytes = file.getBytes();
            String fileName = file.getOriginalFilename();
            StringUtils.substringAfter(fileName, ".");
            File tmpDir = new File(request.getServletContext().getRealPath("/tmp/"));
            if (!tmpDir.exists()) {
                tmpDir.mkdir();
            }
            File writeFile = new File(tmpDir, id + fileName.substring(fileName.lastIndexOf(".")));
            randomAccessFile = new RandomAccessFile(writeFile, "rw");
            randomAccessFile.seek(Long.valueOf(rangeHeader.split("=")[1].split("-")[0]));
            randomAccessFile.write(bytes);
            response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes="+randomAccessFile.length());
            randomAccessFile.close();
        } catch (InvalidPathException e) {
            logger.error(e.getMessage(), e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        } finally {
            if (randomAccessFile != null)
                randomAccessFile.close();
        }
    }

    /**
     * 上传完毕后通知服务器
     * @param id
     * @param fileName
     * @param messageDO
     * @param request
     */
    @PostMapping("/uploadDone")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadDone(@RequestParam String id, @RequestParam String fileName, @RequestBody MessageDO messageDO, HttpServletRequest request) {
        final String tmpDir = request.getServletContext().getRealPath("/tmp/");
        executor.submit(() -> {
            try {
                logger.info("{} - upload done", fileName);
                Path path = Paths.get(tmpDir + id + fileName.substring(fileName.lastIndexOf(".")));
                File file = path.toFile();
                FileInputStream in = new FileInputStream(file);
                String contentType = Files.probeContentType(path);
                if (StringUtils.isEmpty(contentType)) {
                    contentType = "application/octet-stream";
                }
                ObjectId objectId = gridFsTemplate.store(in, fileName, contentType);
                messageDO.setContent(objectId.toString());
                GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(objectId)));
                messageDO.setFileDetail(new FileDetail(gridFSFile.getLength(), gridFSFile.getFilename(),
                        gridFSFile.getMetadata()==null?"":gridFSFile.getMetadata().getString("_contentType")));
                producer.sendChatMessage(JSON.toJSONString(messageDO), messageDO.getConversationId().hashCode() & Integer.MAX_VALUE);
                in.close();
                file.delete();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
    }

    /**
     * 文件下载
     * @param id
     * @param response
     * @throws IOException
     */
    @GetMapping("download")
    public void download(String id, HttpServletResponse response) throws IOException {
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(id)));
        response.setContentType(gridFSFile.getMetadata().getString("_contentType"));
        response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(gridFSFile.getLength()));
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+ URLEncoder.encode(gridFSFile.getFilename(), "utf-8"));
        ServletOutputStream outputStream = response.getOutputStream();
        GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDbFactory.getDb());
        gridFSBucket.downloadToStream(gridFSFile.getObjectId(), outputStream);
        outputStream.close();
    }
}
