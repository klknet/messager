package com.konglk.ims.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ImsSpringMVCConfig
  implements WebMvcConfigurer
{
  @Autowired
  private AuthenticationInterceptor authenticationInterceptor;
  
  public void addInterceptors(InterceptorRegistry registry)
  {
    registry.addInterceptor(this.authenticationInterceptor)
            .addPathPatterns(new String[] { "/user/*" ,"/conversation/*", "/message/*", "/relation/*"})
            .excludePathPatterns(new String[] {"/user/login"});
  }
}
