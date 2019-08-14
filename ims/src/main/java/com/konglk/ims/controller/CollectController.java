package com.konglk.ims.controller;

import com.konglk.ims.service.CollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by konglk on 2019/8/14.
 */
@RestController
@RequestMapping("/collect")
public class CollectController {

    @Autowired
    private CollectService collectService;

    @PostMapping("save")
    public void collect(String userId, String msgId) {
        collectService.insert(userId, msgId);
    }

    @GetMapping("list")
    public Object list(String userId) {
        return collectService.list(userId);
    }


}
