package com.konglk.ims.controller;

import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.service.MessageService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.*;
import reactor.util.function.Tuple2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

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
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    private Queue<Pair<String, String>> unreadQueue = new ConcurrentLinkedDeque<>();

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
        unreadQueue.add(Pair.of(userId, id));
    }

    @DeleteMapping("/delMsg")
    public void delMsg(String msgId, String userId) {
        messageService.delByMsgId(msgId, userId);
    }


    @Scheduled(cron = "* */1 * * * *")
    public void batchUnread() {
        if (unreadQueue.size() > 0) {
            List<Pair<String, String>> unread = new ArrayList<>(unreadQueue);
            unreadQueue.removeAll(unread);
            Map<String, Set<String>> map = new HashMap<>(unread.size());
            for (Pair<String, String> tuple2: unread) {
                if (map.containsKey(tuple2.getLeft())) {
                    map.get(tuple2.getLeft()).add(tuple2.getRight());
                }else {
                    Set<String> set = new HashSet<>();
                    set.add(tuple2.getRight());
                    map.put(tuple2.getLeft(), set);
                }
            }
            for (String userId: map.keySet()) {
                for (String id: map.get(userId)) {
                    redisCacheService.delUnreadNum(userId, id);
                }
            }
        }
    }


}
