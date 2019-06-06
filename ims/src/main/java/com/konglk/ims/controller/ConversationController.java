package com.konglk.ims.controller;

import com.konglk.ims.domain.ConversationDO;
import com.konglk.ims.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/conversation"})
public class ConversationController {
    @Autowired
    private ConversationService conversationService;

    @PostMapping({"/build"})
    public ConversationDO build(String userId, String destId) {
        return this.conversationService.buildConversation(userId, destId);
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
