package com.konglk.ims;

import com.konglk.ims.cache.Constants;
import com.konglk.ims.domain.ConfigDO;
import com.konglk.ims.domain.UserDO;
import com.konglk.ims.repo.IConfigRepository;
import com.konglk.ims.service.ConfigService;
import com.konglk.ims.service.ConversationService;
import com.konglk.ims.service.UserService;
import com.konglk.ims.util.EncryptUtil;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by konglk on 2019/5/23.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class UserTest {

    @Autowired
    private UserService userService;
    @Autowired
    private ConversationService conversationService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private IConfigRepository configRepository;

    private String[] words = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
    "s", "t", "u", "v", "w", "x", "y", "z"};
    private int[] numbers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private String[] city = {"武汉","仙桃","荆州","北京","上海","深圳","广州","天津","石家庄","西安","郑州",
            "洛阳","杭州","南京","苏州",};


    @Test
    public void test() {
        System.out.println(userService.findByUserId("780cc721-c9c8-4d95-a428-cf33a74e5b88"));
    }

    public void batchAddFriend(int friendsNum, Date time) {
        List<UserDO> userDOS = null;
        int page = 0;
        do {
            userDOS = userService.findUserByPage(page, friendsNum, time);
            page += 1;
            if(CollectionUtils.isEmpty(userDOS)) {
                break;
            }
            List<String> exclude = Arrays.asList("konglk","qintian","maomao");
            for(int i=0; i<userDOS.size(); i+=friendsNum) {
                int j=i+friendsNum;
                j = Math.min(j, userDOS.size());
                for (int k=i; k<j; k++) {
                    UserDO userDO = userDOS.get(k);
                    if (exclude.contains(userDO.getUsername()))
                        continue;
                    List<UserDO> friends = userDOS.stream().filter(u -> !u.getUserId().equals(userDO.getUserId())).collect(Collectors.toList());
                    if(CollectionUtils.isEmpty(friends))
                        continue;
                    userService.batchAddFriend(userDO.getUserId(), friends);
//                    conversationService.batchConversation(userDO.getUserId(), friends.stream().map(obj -> obj.getUserId()).collect(Collectors.toList()));
                }
            }

        }while (!CollectionUtils.isEmpty(userDOS));

    }

    /*
    批量插入256 个用户
     */
    @Test
//    @Transactional
    public void batchInsert() {
        ConfigDO configDO = configRepository.findByName(Constants.CONFIG_TEST_USER_NUMBER);
        ConfigDO configDOSeq = configRepository.findByName(Constants.CONFIG_TEST_USER_SEQUENCE);
        Date time = new Date();
        int n = Integer.parseInt(configDO.getValue());
        int base = Integer.parseInt(configDOSeq.getValue());
        List<UserDO> users = new ArrayList<>();
//        String[] username = genUsername(n);
        String[] username = getUsername(base, n);
        String[] cellphone = genCellphone(n);
        String[] email = genEmail(n);
        Random random = new Random();

//        List<String> profileIds = new ArrayList<>();
//        GridFSFindIterable gridFSFiles = gridFsTemplate.find(new Query());
//        MongoCursor<GridFSFile> iterator = gridFSFiles.iterator();
//        while (iterator.hasNext()) {
//            GridFSFile next = iterator.next();
//            profileIds.add(next.getObjectId().toString());
//        }
        for (int i=0; i<n; i++) {
            UserDO userDO = new UserDO();
            userDO.setUsername(username[i]);
            userDO.setAge(18+random.nextInt(Integer.MAX_VALUE)%37);
            userDO.setCellphone(cellphone[i]);
            userDO.setMailbox(email[i]);
            userDO.setGender(random.nextInt(Integer.MAX_VALUE)&1);
//            userDO.setNickname(NameRandomUtil.getRandomJianHan(3+random.nextInt(Integer.MAX_VALUE)%3));
            userDO.setNickname("user-"+username[i]);
//            userDO.setSignature(NameRandomUtil.getRandomJianHan(5+random.nextInt(Integer.MAX_VALUE)%7));
            userDO.setSignature("signature-"+username[i]);
            userDO.setRawPwd(EncryptUtil.encrypt(username[i]));
            userDO.setCity(city[random.nextInt(city.length)]);
            userDO.setCountry("China");
//            userDO.setProfileUrl(profileIds.get(random.nextInt(Integer.MAX_VALUE)%profileIds.size()));
            users.add(userDO);
        }
        userService.batchInsert(users);
//        batchAddFriend(n, time);
        configService.updateConfigValue(Constants.CONFIG_TEST_USER_SEQUENCE, base+n+"");
    }

    @Test
    public void addFriend() {
        LocalDateTime from = LocalDateTime.of(2019, 11, 13, 0, 0, 0);
        batchAddFriend(256, Date.from(from.atZone(ZoneId.systemDefault()).toInstant()));
    }

    @Test
    public void updateConfig() {
        ConfigDO configDO = configRepository.findByName(Constants.CONFIG_TEST_USER_NUMBER);
        ConfigDO configDOSeq = configRepository.findByName(Constants.CONFIG_TEST_USER_SEQUENCE);
        int n = Integer.parseInt(configDO.getValue());
        int base = Integer.parseInt(configDOSeq.getValue());
        configService.updateConfigValue(Constants.CONFIG_TEST_USER_SEQUENCE, base+n+"");
    }

    @Test
    public void updateRemark() {
        userService.setFriendNotename("9bafc220-1f9a-47a6-937d-ce3d200a6380", "8e56093a-f065-48ec-b667-4f0d6e092ddc", "user-1000086");
    }

    private String[] getUsername(int base, int n) {
        String[] usernames = new String[n];
        for (int i=0; i<n; i++) {
            usernames[i] = ""+base++;
        }
        return usernames;
    }

    private String[] genCellphone(int n) {
        Set<String> cellphone = new HashSet<>();
        int k=0;
        Random random = new Random();
        String[] prefix = {"185", "180", "134", "159", "186", "152", "188"};
        while (k<n) {
            String phone = prefix[random.nextInt(Integer.MAX_VALUE) % prefix.length];
            for (int i=0; i<8; i++) {
                phone += numbers[random.nextInt(10)];
            }
            boolean add = cellphone.add(phone);
            if (add)
                k+=1;
        }
        System.out.println("cellphone"+k);
        return cellphone.toArray(new String[n]);
    }

    private String[] genEmail(int n) {
        Set<String> emails = new HashSet<>();
        int k=0;
        Random random = new Random();
        String[] suffix = {"@aliyun.com", "@gmail.com", "@163.com", "@qq.com", "@sina.com", "@127.com", "@sohu.com"};
        while (k<n) {
            String name = "";
            for (int i=0; i<6+random.nextInt(6); i++) {
                name += words[random.nextInt(26)];
            }
            boolean add = emails.add(name + suffix[random.nextInt(Integer.MAX_VALUE) % suffix.length]);
            if (add)
                k+=1;
        }
        System.out.println("emails"+k);
        return emails.toArray(new String[n]);
    }

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Test
    public void insertImg() throws Exception {
        String mv = "d:\\美女";
        String sg = "d:\\帅哥";
        File file = new File(mv);
        File[] files = file.listFiles();
        FileInputStream in;
        for(File f: files) {
            in = new FileInputStream(f);
            gridFsTemplate.store(in, f.getName(), "image/jpg");
            in.close();
        }
        file = new File(sg);
        files = file.listFiles();
        for(File f: files) {
            in = new FileInputStream(f);
            gridFsTemplate.store(in, f.getName(), "image/jpg");
            in.close();
        }
    }


}
