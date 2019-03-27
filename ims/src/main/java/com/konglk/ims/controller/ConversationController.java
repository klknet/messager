package com.konglk.ims.controller;

import com.konglk.ims.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/conversation"})
public class ConversationController
{
  @Autowired
  private ConversationService conversationService;
  
  @PostMapping({"/build"})
  public void build(String userId, String destId)
  {
    this.conversationService.buildConversation(userId, destId);
  }
  
  @GetMapping({"/list"})
  public Object listConversation(@RequestParam String userId)
  {
    return this.conversationService.listConversation(userId);
  }
}
