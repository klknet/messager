package com.konglk.ims;

import com.konglk.ims.cache.Constants;
import com.konglk.ims.ws.ChatEndPoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@SpringBootApplication
@EnableScheduling
public class ImsApplication
        extends SpringBootServletInitializer {

    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(new Class[]{ImsApplication.class});
    }

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(ImsApplication.class, args);
    }

    @Bean
    @Profile(Constants.DEV)
    public ServerEndpointExporter serverEndpointExporter() {
        ServerEndpointExporter serverEndpointExporter = new ServerEndpointExporter();
        serverEndpointExporter.setAnnotatedEndpointClasses(ChatEndPoint.class);
        return serverEndpointExporter;
    }
}
