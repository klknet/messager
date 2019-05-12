package com.konglk.ims.service;

import com.konglk.ims.cache.Constants;
import com.konglk.ims.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by konglk on 2019/5/3.
 */
@Component
public class RandomQueueName {

    private int queueNum = 5;
    private String prdBase = "ims_chat_";
    private String devBase = "ims_chat_dev_";

    @Autowired
    private SpringUtils springUtils;

    public String getQueueName() {
        return base() + (System.currentTimeMillis() % queueNum);
    }

    public String[] queues() {
        String[] names = new String[queueNum];
        int i = queueNum;
        while (i-- > 0){
            names[i] = base()+i;
        }
        return names;
    }

    private String base() {
        return springUtils.existProfile(Constants.DEV) ? devBase : prdBase;
    }

    public int getQueueNum() {
        return queueNum;
    }

}
