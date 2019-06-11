package com.konglk.ims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Created by konglk on 2019/6/10.
 */
@Component
public class FileManager {
    @Autowired
    private GridFsTemplate gridFsTemplate;

//    private String store(InputStream in, )

}
