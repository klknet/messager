package com.konglk.ims;

import com.konglk.ims.util.EncryptUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by konglk on 2019/6/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
public class CommonTest {
    @Test
    public void aes() {
        String text = "hello world";
        String cipher = EncryptUtil.encrypt(text);
        System.out.println(cipher);
        System.out.println(EncryptUtil.decrypt(cipher));
        System.out.println(EncryptUtil.decrypt("8B40AD44264014711E6A3C67E4E5EF44"));
    }
}
