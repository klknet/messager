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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

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

    private void
    connect(String unique, String pwd) {
        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("unique", unique);
        request.add("pwd", EncryptUtil.encrypt(pwd));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        httpHeaders.addAll(request);
        HttpEntity entity = new HttpEntity(httpHeaders);
        UserDO userDO = restTemplate.postForObject("http://192.168.1.100/ims/user/login", request, UserDO.class);
        try {
            if(userDO == null)
                return;
            System.out.println(userDO.getUsername());
            final WebsocketClient client =
                    new WebsocketClient(new URI("ws://192.168.1.100/ims/ws/chat?userId="+userDO.getUserId()+"&ticket="+userDO.getTicket()), userDO);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void groupChat() {
        ConfigDO seq = configService.getConfigByName(Constants.CONFIG_TEST_USER_SEQUENCE);
        ConfigDO num = configService.getConfigByName(Constants.CONFIG_TEST_USER_NUMBER);
        int end = Integer.parseInt(seq.getValue());
        int start = Integer.parseInt(num.getValue());
        for(int i=end-start; i<end-3; i++) {
            final String unique = ""+i;
            connect(unique, unique);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        while (true) {
            Calendar c = Calendar.getInstance();
            if (c.after(calendar))
                break;
        }
        System.out.println("#######end##########3"+new Date());
    }

}
