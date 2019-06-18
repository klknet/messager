package com.konglk.ims.controller;

import com.konglk.ims.cache.RedisCacheService;
import com.konglk.ims.domain.ConversationDO;
import com.konglk.ims.domain.GroupChatDO;
import com.konglk.ims.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping({"/conversation"})
public class ConversationController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ConversationService conversationService;
    @Autowired
    private RedisCacheService redisCacheService;


    @PostMapping({"/build"})
    public ConversationDO build(String userId, String destId) {
        return this.conversationService.buildConversation(userId, destId);
    }

    /*
    创建群聊会话
     */
    @PostMapping("/groupChat")
    public void groupChat(String userId, String userIds, String notename) throws IOException {
         conversationService.groupConversation(userId, Arrays.asList(userIds.split(",")), notename);
    }

    @GetMapping({"/list"})
    public Object listConversation(@RequestParam String userId) {
        return this.conversationService.listConversation(userId);
    }

    @DeleteMapping("/delete")
    public void deleteConversation(String conversationId, String userId) {
        conversationService.delete(conversationId, userId);
        redisCacheService.delUnreadNum(userId, conversationId);
    }

    /*
    置顶
     */
    @PostMapping("/top")
    public void top(String conversationId, String userId, boolean top) {
        conversationService.topConversation(userId, conversationId, top);
    }

    /*
    消息免打扰
     */
    @PostMapping("/dnd")
    public void dnd(String conversationId, String userId, boolean dnd) {
        conversationService.dndConversation(userId, conversationId, dnd);
    }

    /*
    群聊成员
     */
    @GetMapping("/groupChatMember")
    public GroupChatDO groupChatMember(String id) {
        return conversationService.findGroupChat(id);
    }

    @GetMapping("/test")
    public Object test(@RequestParam String id) {
        return "id=" + id + System.currentTimeMillis();
    }
}
