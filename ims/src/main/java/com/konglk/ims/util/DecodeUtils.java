package com.konglk.ims.util;

import java.util.Base64;
import java.util.Base64.Decoder;

public class DecodeUtils {
    public static String decode(String src, String salt) {
        String raw = new String(Base64.getDecoder().decode(src.getBytes()));
        return raw.substring(salt.length());
    }
}
