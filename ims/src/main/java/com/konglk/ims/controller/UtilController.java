package com.konglk.ims.controller;

import com.konglk.ims.util.EncryptUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by konglk on 2019/6/6.
 */
@RestController
@RequestMapping("/util")
public class UtilController {


    @GetMapping("/aesKey")
    public Object aesKey() {
        Map<String,String> data = new HashMap<>();
        data.put("key", EncryptUtil.getAesKey());
        data.put("iv", EncryptUtil.getIv());
        return data;
    }
}
