package com.konglk.ims.controller;

import com.konglk.ims.service.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by konglk on 2019/4/23.
 */
@RestController
@RequestMapping("/relation")
public class RelationController {
    @Autowired
    private RelationService relationService;

    @PostMapping("/requestFriend")
    public void requestFriend(String userId, String destId, String note) {
        relationService.requestFriend(userId, destId, note);
    }

    @PutMapping("/agreeRequest")
    public void agreeRequest(String userId, String destId) {
        relationService.agreeRequest(userId, destId);
    }

    @PutMapping("/refuseRequest")
    public void refuseRequest (String userId, String destId) {
        relationService.refuseRequest(userId, destId);
    }

    @GetMapping("/requestList")
    public Object requestList(@RequestParam String userId) {
        return relationService.requestList(userId);
    }

}
