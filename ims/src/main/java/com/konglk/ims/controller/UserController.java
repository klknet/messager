package com.konglk.ims.controller;

import com.konglk.ims.domain.UserDO;
import com.konglk.ims.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping({"/user"})
public class UserController {
    @Autowired
    private UserService userService;

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
}
