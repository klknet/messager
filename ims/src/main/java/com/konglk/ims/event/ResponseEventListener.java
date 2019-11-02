package com.konglk.ims.event;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.domain.MessageDO;
import com.konglk.ims.service.NotifyService;
import com.konglk.ims.ws.ChatEndPoint;
import com.konglk.ims.ws.PresenceManager;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by konglk on 2019/6/12.
 */
@Component
public class ResponseEventListener implements ApplicationListener<ResponseEvent> {

    @Autowired
    private PresenceManager presenceManager;
    @Autowired
    private NotifyService notifyService;
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    @Autowired
    private RedisCacheService cacheService;
    private Logger logger = LoggerFactory.getLogger(getClass());

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
                                try {
                                    Object msgResponse = cacheService.getMsgResponse(messageDO.getMessageId(), id);
                                    if (msgResponse != null && presenceManager.existsUser(id)) {
                                        presenceManager.getClient(id).send(response);
                                    }
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                            }
                        }, DateUtils.addSeconds(new Date(), 10));
                    }
                    ChatEndPoint client = presenceManager.getClient(id);
                    client.send(response);
                }
            }
        }
    }
}
