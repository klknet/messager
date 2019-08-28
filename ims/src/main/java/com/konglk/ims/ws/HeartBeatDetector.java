package com.konglk.ims.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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

    private final long liveTime = 1000*2*60;

    @Scheduled(cron = "0 */1 * * * *")
    public void detect() {
        logger.info("begin remove timeout connection");
        Map<String, ChatEndPoint> clientMap = presenceManager.getClientMap();
        if (clientMap != null && clientMap.size()>0) {
            Iterator<Map.Entry<String, ChatEndPoint>> iterator = clientMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, ChatEndPoint> entry = iterator.next();
                long cur = System.currentTimeMillis();
                if (Math.abs(cur - entry.getValue().getTimestamp()) > liveTime) {
                    try {
                        entry.getValue().getSession().close();
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                    logger.info("remove timeout connection {}", entry.getValue().getNickname());
                    iterator.remove();
                }
            }
        }
    }
}
