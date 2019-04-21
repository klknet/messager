package com.konglk.ims.controller;

import com.konglk.ims.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/conversation"})
public class ConversationController {
    @Autowired
    private ConversationService conversationService;

    @PostMapping({"/build"})
    public void build(String userId, String destId) {
        this.conversationService.buildConversation(userId, destId);
    }

    @GetMapping({"/list"})
    public Object listConversation(@RequestParam String userId) {
        return this.conversationService.listConversation(userId);
    }

    @DeleteMapping("/delete")
    public void deleteConversation(String conversationId, String userId) {
        conversationService.delete(conversationId, userId);
    }

    @GetMapping("/test")
    public Object test(@RequestParam String id) {
        return "id=" + id + System.currentTimeMillis();
    }
}
