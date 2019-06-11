package com.konglk.ims.controller;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
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

    @GetMapping("/img")
    public void getImg(@RequestParam String id, HttpServletResponse response) throws IOException {
        //TODO 可以304优化
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(id)));
        if(gridFSFile != null) {
            Document metadata = gridFSFile.getMetadata();
            response.setContentType(metadata.getString("_contentType"));
            ServletOutputStream outputStream = response.getOutputStream();
            GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDbFactory.getDb());
            gridFSBucket.downloadToStream(gridFSFile.getObjectId(), outputStream);
            outputStream.close();
        }

    }
}
