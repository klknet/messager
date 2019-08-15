package com.konglk.ims.config;

import com.konglk.ims.util.EncryptUtil;
import com.konglk.ims.ws.PresenceManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

//    private static final String TOKEN = "6c766178-4eef-11e9-89c1-40a3cc5c760e";
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PresenceManager presenceManager;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String userId = EncryptUtil.decrypt(request.getHeader("userId"));
        String ticket = EncryptUtil.decrypt(request.getHeader("ticket"));
        if (StringUtils.isNotEmpty(userId) && StringUtils.isNotEmpty(ticket)
                && ticket.equals(presenceManager.getTicket(userId))) {
            return true;
        }
        this.logger.info("verification failed, error token");
        response.sendError(HttpStatus.FORBIDDEN.value(), "error token");
        return false;
    }
}
