package com.konglk.ims.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ImsSpringMVCConfig
  implements WebMvcConfigurer
{
  @Autowired
  private ValidationHandler validationHandler;
  
  public void addInterceptors(InterceptorRegistry registry)
  {
    registry.addInterceptor(this.validationHandler).addPathPatterns(new String[] { "/user/*" });
  }
}
