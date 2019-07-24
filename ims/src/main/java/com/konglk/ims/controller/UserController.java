package com.konglk.ims.controller;

import com.konglk.ims.domain.UserDO;
import com.konglk.ims.event.ResponseEvent;
import com.konglk.ims.service.ConversationService;
import com.konglk.ims.service.UserService;
import com.konglk.ims.util.SpringUtils;
import com.konglk.ims.ws.ConnectionHolder;
import com.konglk.model.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping({"/user"})
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ConnectionHolder connectionHolder;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private SpringUtils springUtils;

    @PostMapping({"/add"})
    public void addUser(@RequestBody UserDO userDO) {
        this.userService.addUser(userDO);
    }

    @PostMapping({"/login"})
    public UserDO findByUsername(@NotNull String unique, @NotNull String pwd) {
        return this.userService.login(unique, pwd);
    }

    @PostMapping({"/addFriend"})
    public UserDO addFriend(String userId, String destId, String remark) {
        return this.userService.addFriend(userId, destId, remark);
    }

    @GetMapping("/find")
    public Object findUser(String username) {
        if(StringUtils.isEmpty(username))
            return null;
        return userService.findUser(username);
    }

    @GetMapping("/findById")
    public UserDO findById(String userId) {
        return this.userService.findByUserId(userId);
    }

    @DeleteMapping("/delTicket")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delTicket(String userId) {
        connectionHolder.removeTicket(userId);
    }

    @PutMapping("/setNotename")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setNotename(String userId, String destId, String notename) {
        userService.setFriendNotename(userId, destId, notename);
        conversationService.updateConversationName(userId, destId, notename);
        ResponseEvent event =
                new ResponseEvent(new Response(com.konglk.model.ResponseStatus.U_UPDATE_NOTENAME, Response.USER), userId);
        springUtils.getApplicationContext().publishEvent(event);
    }
}
