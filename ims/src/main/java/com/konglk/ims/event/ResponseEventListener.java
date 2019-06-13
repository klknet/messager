package com.konglk.ims.event;

import com.alibaba.fastjson.JSON;
import com.konglk.ims.service.NotifyService;
import com.konglk.ims.service.ReplyService;
import com.konglk.ims.ws.ChatEndPoint;
import com.konglk.ims.ws.ConnectionHolder;
import com.konglk.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by konglk on 2019/6/12.
 */
@Component
public class ResponseEventListener implements ApplicationListener<ResponseEvent> {

    @Autowired
    private ConnectionHolder connectionHolder;
    @Autowired
    private ReplyService replyService;
    @Autowired
    private NotifyService notifyService;

    @Override
    public void onApplicationEvent(ResponseEvent event) {
        if(event.getSource() instanceof Response) {
            Response response = (Response) event.getSource();
            String userId = event.getUserId();
            if(userId != null) {
                //在线用户直接推送消息
                if(connectionHolder.getClient(userId) != null) {
                    ChatEndPoint client = connectionHolder.getClient(userId);
                    replyService.reply(client, response);
                }else {
                    //离线用户将数据存入db，等用户上线后再推送。
                    notifyService.saveNotify(userId, JSON.toJSONString(response));
                }
            }
        }
    }
}
