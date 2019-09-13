package com.konglk.ims;

import com.konglk.ims.util.EncryptUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by konglk on 2019/6/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class CommonTest {
    @Test
    public void aes() {
        String text = "hello world";
        String cipher = EncryptUtil.encrypt(text);
        System.out.println(cipher);
        System.out.println(EncryptUtil.decrypt(cipher));
        System.out.println(EncryptUtil.decrypt("8B40AD44264014711E6A3C67E4E5EF44"));
    }
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Test
    public void testGridFS() throws FileNotFoundException {
        File file = new File("D:\\gp\\gpb9.jpg");
        FileInputStream content = new FileInputStream(file);
        DBObject metadata = new BasicDBObject();
        metadata.put("userId", "780cc721-c9c8-4d95-a428-cf33a74e5b88");
        ObjectId store = gridFsTemplate.store(content, file.getName(), "image/jpg", metadata);
        System.out.println(store);
    }
}
