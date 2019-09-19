package com.konglk.ims.event;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.service.NotifyService;
import com.konglk.ims.service.ReplyService;
import com.konglk.ims.ws.ChatEndPoint;
import com.konglk.ims.ws.PresenceManager;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by konglk on 2019/6/12.
 */
@Component
public class ResponseEventListener implements ApplicationListener<ResponseEvent> {

    @Autowired
    private PresenceManager presenceManager;
    @Autowired
    private ReplyService replyService;
    @Autowired
    private NotifyService notifyService;
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    @Autowired
    private RedisCacheService cacheService;

    @Override
    @Async
    public void onApplicationEvent(ResponseEvent event) {
        if(event.getSource() instanceof Response) {
            Response response = (Response) event.getSource();
            String userId = event.getUserId();
            List<String> userIds = event.getUserIds();
            List<String> targetUsers = new ArrayList<>();
            if (userId != null) {
                targetUsers.add(userId);
            }
            if (userIds != null) {
                targetUsers.addAll(userIds);
            }
//            List<String> offlineUsers = new ArrayList(targetUsers.size()/2);
            for (String id: targetUsers) {
                //在线用户直接推送消息
                if(presenceManager.getClient(id) != null) {
                    if (response.getType() == 2 && response.getCode() == ResponseStatus.M_TRANSFER_MESSAGE.getCode()) {
                        String data = response.getData();
                        MessageDO messageDO = JSON.parseObject(data, MessageDO.class);
                        cacheService.setMsgResponse(messageDO.getMessageId(), id);
                        //10秒无ack会重传
                        taskScheduler.schedule(new Runnable() {
                            @Override
                            public void run() {
                                Object msgResponse = cacheService.getMsgResponse(messageDO.getMessageId(), id);
                                if (msgResponse != null) {
                                    replyService.reply(presenceManager.getClient(id), response);
                                }
                            }
                        }, DateUtils.addSeconds(new Date(), 10));
                    }
                    ChatEndPoint client = presenceManager.getClient(id);
                    replyService.reply(client, response);
                } else {
//                    offlineUsers.add(id);
                }
            }

//            if (offlineUsers.size() > 0) {
//                List<Boolean> status = cacheService.isOnline(offlineUsers);
//                List<String> offline = new ArrayList<>();
//                for (int i=0; i<offlineUsers.size(); i++) {
//                    if (BooleanUtils.isFalse(status.get(i))) {
//                        offline.add(offlineUsers.get(i));
//                    }
//                }
//                if (offline.size() > 0) {
//                    notifyService.saveAllNotify(offline, JSON.toJSONString(response));
//                }
//            }
        }
    }
}
