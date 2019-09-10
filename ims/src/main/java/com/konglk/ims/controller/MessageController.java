package com.konglk.ims.controller;

import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.service.MessageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    /*
    取晚于convCreateTime后早于lastMsgCreateTime消息
     */
    @GetMapping("/prev")
    public Object prev(String cid, String userId, String start, String end, boolean include) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        return messageService.prevMessages(cid, userId, sdf.parse(start),
                StringUtils.isEmpty(end) ? null : sdf.parse(end), include);
    }

    /*
    撤回消息，2分钟以内的消息可撤回
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/revocation")
    public void revocation(String userId, String msgId) {
        messageService.revocation(userId, msgId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/delUnread")
    public void delUnread(String userId, String id) {
        redisCacheService.delUnreadNum(userId, id);
    }

    @DeleteMapping("/delMsg")
    public void delMsg(String msgId, String userId) {
        messageService.delByMsgId(msgId, userId);
    }


}
