package com.konglk.ims.service;

import com.konglk.ims.cache.Constants;
import com.konglk.ims.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by konglk on 2019/5/3.
 */
@Component
public class RandomTopicName {

    private int topicNum = 5;
    private String prdBase = "topic_ims_chat_";
    private String devBase = "topic_ims_chat_dev_";

    @Autowired
    private SpringUtils springUtils;


    public String getTopicName(String route) {
        return base() + Math.abs(route.hashCode() % topicNum);
    }

    public String[] topics() {
        String[] names = new String[topicNum];
        int i = topicNum;
        while (i-- > 0){
            names[i] = base()+i;
        }
        return names;
    }

    private String base() {
        return springUtils.existProfile(Constants.DEV) ? devBase : prdBase;
    }

}
