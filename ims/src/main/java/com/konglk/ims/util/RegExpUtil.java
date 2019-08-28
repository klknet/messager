package com.konglk.ims.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by konglk on 2019/8/28.
 */
public class RegExpUtil {

    public static String getUrlParameter(String url, String id) {
        String reg = "(^|&)"+id+"=(?<"+id+">[^&]*)(&|$)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(url);
        if(matcher.find()) {
            return matcher.group(id);
        }
        return null;
    }
}
