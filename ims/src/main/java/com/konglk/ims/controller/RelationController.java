package com.konglk.ims.controller;

import com.konglk.ims.service.RelationService;
import com.konglk.ims.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Created by konglk on 2019/4/23.
 */
@RestController
@RequestMapping("/relation")
public class RelationController {
    @Autowired
    private RelationService relationService;
    @Autowired
    private UserService userService;

    @PostMapping("/requestFriend")
    public void requestFriend(String userId, String destId, String note) {
        relationService.requestFriend(userId, destId, note);
    }

    @PostMapping("/agreeRequest")
    public void agreeRequest(String objectId, String userId) {
        relationService.agreeRequest(objectId, userId);
    }

    @PutMapping("/refuseRequest")
    public void refuseRequest(String objectId, String userId) {
        relationService.refuseRequest(objectId, userId);
    }

    @GetMapping("/requestList")
    public Object requestList(@RequestParam String userId) {
        return relationService.requestList(userId);
    }

    /*
   删除好友
    */
    @DeleteMapping("/delFriend")
    public void delFriend(@RequestParam String userId, @RequestParam String friendId) {
        userService.delFriend(userId, friendId);
    }

}
