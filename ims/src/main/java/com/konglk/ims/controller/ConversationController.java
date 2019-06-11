package com.konglk.ims.controller;

import com.konglk.ims.domain.ConversationDO;
import com.konglk.ims.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping({"/conversation"})
public class ConversationController {
    @Autowired
    private ConversationService conversationService;

    @PostMapping({"/build"})
    public ConversationDO build(String userId, String destId) {
        return this.conversationService.buildConversation(userId, destId);
    }

    @PostMapping("/groupChat")
    public ConversationDO groupChat(String userId, String userIds, String notename) throws IOException {
        return conversationService.groupConversation(userId, Arrays.asList(userIds.split(",")), notename);
    }

    @GetMapping({"/list"})
    public Object listConversation(@RequestParam String userId) {
        return this.conversationService.listConversation(userId);
    }

    @DeleteMapping("/delete")
    public void deleteConversation(String conversationId, String userId) {
        conversationService.delete(conversationId, userId);
    }

    @PostMapping("/top")
    public void top(String conversationId, String userId, boolean top) {
        conversationService.topConversation(userId, conversationId, top);
    }

    @PostMapping("/dnd")
    public void dnd(String conversationId, String userId, boolean dnd) {
        conversationService.dndConversation(userId, conversationId, dnd);
    }

    @GetMapping("/test")
    public Object test(@RequestParam String id) {
        return "id=" + id + System.currentTimeMillis();
    }
}
