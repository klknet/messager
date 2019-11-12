package com.konglk.ims;

import com.konglk.ims.cache.Constants;
import com.konglk.ims.domain.ConfigDO;
import com.konglk.ims.domain.UserDO;
import com.konglk.ims.service.ConfigService;
import com.konglk.ims.service.UserService;
import com.konglk.ims.util.EncryptUtil;
import com.konglk.ims.ws.WebsocketClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class WebsocketClientTest {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UserService userService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    @Value("${host}")
    private String host;

    private WebsocketClient connect(String unique, String pwd) {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("unique", unique);
        request.add("pwd", EncryptUtil.encrypt(pwd));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        httpHeaders.addAll(request);
        UserDO userDO = restTemplate.postForObject("http://"+host+"/ims/user/login", request, UserDO.class);
        try {
            if(userDO == null)
                return null;
            final WebsocketClient client =
                    new WebsocketClient(new URI("ws://"+host+"/ims/ws/chat?userId="+userDO.getUserId()+"&ticket="+userDO.getTicket()), userDO);
            return client;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void groupChat() {
        ConfigDO seq = configService.getConfigByName(Constants.CONFIG_TEST_USER_SEQUENCE);
        ConfigDO num = configService.getConfigByName(Constants.CONFIG_TEST_USER_NUMBER);
        int end = Integer.parseInt(seq.getValue());
        int start = Integer.parseInt(num.getValue());
        List<WebsocketClient> clients = new ArrayList<>();
        for(int i=end-start; i<end-3; i++) {
            final String unique = ""+i;
            taskExecutor.submit(() -> clients.add(connect(unique, unique)));
//            connect(unique, unique);
        }
        long s = System.currentTimeMillis();
        while (true) {
            long e = System.currentTimeMillis();
            if (e-s > 1000*60*10)
                break;
            else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        clients.forEach(client -> {
            if (client != null)
                client.release();
        });
        System.out.println("#######end##########3"+new Date());
    }

}
