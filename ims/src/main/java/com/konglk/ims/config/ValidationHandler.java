package com.konglk.ims.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ValidationHandler
        implements HandlerInterceptor {
    private static final String TOKEN = "6c766178-4eef-11e9-89c1-40a3cc5c760e";
    private Logger logger = LoggerFactory.getLogger(getClass());

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String token = request.getHeader("token");
        if (StringUtils.equals("6c766178-4eef-11e9-89c1-40a3cc5c760e", token)) {
            return true;
        }
        this.logger.debug("verification failed, error token");
        return false;
    }
}
