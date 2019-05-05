package com.konglk.ims.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created by konglk on 2019/5/3.
 */
@Component
public class RandomQueueName {

    private int queueNum = 5;
    private String base = "ims_chat_";

    public String getQueueName() {
        return base + (System.currentTimeMillis() % queueNum);
    }

    public String[] queues() {
        String[] names = new String[queueNum];
        int i = queueNum;
        while (i-- > 0){
            names[i] = base+i;
        }
        return names;
    }

}
