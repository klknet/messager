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

    private int topicNum = 2; //must be power of 2.
    private String chat = "ims.chat.";
    private String notify = "ims.notify";

    @Autowired
    private SpringUtils springUtils;


    public String getChatName(int hash) {
        return chat + (hash & (topicNum-1));
    }

    public String getNotifyName() {
        return notify;
    }

    public String[] topics() {
        String[] names = new String[topicNum];
        int i = topicNum;
        while (i-- > 0){
            names[i] = chat+i;
        }
        return names;
    }


}
