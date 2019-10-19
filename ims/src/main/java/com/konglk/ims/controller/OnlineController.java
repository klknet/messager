package com.konglk.ims.controller;

import com.konglk.ims.ws.PresenceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by konglk on 2019/10/19.
 */
@RestController
@RequestMapping("/online")
public class OnlineController {

    @Autowired
    private PresenceManager presenceManager;

    @GetMapping("/users")
    public Object users() {
        return presenceManager.getClientMap().keySet();
    }
}
