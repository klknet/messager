package com.konglk.ims.service;

import com.konglk.ims.cache.Constants;
import com.konglk.ims.util.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by konglk on 2019/5/3.
 */
@Component
public class TopicNameManager {

    private int topicNum = 3;
    private String chatPrd = "topic_ims_chat_";
    private String chatDev = "topic_ims_chat_dev_";
    private String notifyPrd = "topic_ims_notify_";
    private String notifyDev = "topic_ims_notify_";

    @Autowired
    private SpringUtils springUtils;


    public String getChatName(String route) {
        return chatBase() + Math.abs(route.hashCode() % topicNum);
    }

    public String getNotifyName() {
        return notifyBase();
    }

    public String[] topics() {
        String[] names = new String[topicNum];
        int i = topicNum;
        while (i-- > 0){
            names[i] = chatBase()+i;
        }
        return names;
    }

    private String chatBase() {
        return springUtils.existProfile(Constants.DEV) ? chatDev : chatPrd;
    }

    private String notifyBase() {
        return springUtils.existProfile(Constants.DEV) ? notifyDev : notifyPrd;
    }

}
