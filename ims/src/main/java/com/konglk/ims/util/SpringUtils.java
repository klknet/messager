package com.konglk.ims.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by konglk on 2018/8/13.
 */
@Component
public class SpringUtils implements ApplicationContextAware {

  private ApplicationContext applicationContext;
  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    if(this.applicationContext == null) {
      this.applicationContext = applicationContext;
      context = applicationContext;
    }
  }

  public Object getBean(String name) {
    return applicationContext.getBean(name);
  }

  public <T> T getBean(Class<T> clazz) {
    return applicationContext.getBean(clazz);
  }

  public static <T> T getBeanObj(Class<T> clazz) {
    return context.getBean(clazz);
  }

  public boolean existProfile(String profile) {
    return applicationContext.getEnvironment().acceptsProfiles(profile);
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  public static HttpServletRequest getRequest() {
    return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
  }
}
