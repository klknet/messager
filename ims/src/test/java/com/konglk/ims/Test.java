package com.konglk.ims;

import com.alibaba.fastjson.JSON;

import java.util.Map;

/**
 * Created by konglk on 2019/11/4.
 */
public class Test {

    public static void main(String[] args) {
        String val = "{\"url\":\"https://localhost:80\"}";
        Map<String, String> map = JSON.parseObject(val, Map.class);
        System.out.println(map);
    }
}
