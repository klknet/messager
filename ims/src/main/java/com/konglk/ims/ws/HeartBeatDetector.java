package com.konglk.ims.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by konglk on 2019/8/28.
 */
@Service
public class HeartBeatDetector {
    @Autowired
    private PresenceManager presenceManager;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final long liveTime = 120000L;

    @Scheduled(cron = "0 */1 * * * *")
    public void detect() {
        Map<String, ChatEndPoint> clientMap = presenceManager.getClientMap();
        if (clientMap != null && clientMap.size()>0) {
            Iterator<Map.Entry<String, ChatEndPoint>> iterator = clientMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ChatEndPoint> entry = iterator.next();
                long cur = System.currentTimeMillis();
                if (Math.abs(cur - entry.getValue().getTimestamp()) > liveTime) {
                    ChatEndPoint client = entry.getValue();
                    logger.info("remove timeout connection {}", client.getNickname());
                    //移除在线状态，关闭client连接
                    iterator.remove();
                    client.release();
                }
            }
        }
    }
}
