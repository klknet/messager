package com.konglk.ims.controller;

import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by konglk on 2019/4/20.
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private RedisCacheService redisCacheService;

    @GetMapping("/prev")
    public Object prev(String cid, String createtime, boolean include) throws ParseException {
        return messageService.prevMessages(cid, new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(createtime), include);
    }

    @DeleteMapping("/delUnread")
    public void delUnread(String userId, String id) {
        redisCacheService.delUnreadNum(userId, id);
    }



}
