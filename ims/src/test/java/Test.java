import com.alibaba.fastjson.JSON;
import com.konglk.ims.event.ResponseEvent;
import com.konglk.model.Response;
import com.konglk.model.ResponseStatus;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by konglk on 2019/4/15.
 */
public class Test {

    public static void main(String[] args) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse("2019-03-26T11:42:19.344+0000");
            System.out.println(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ResponseEvent e = new ResponseEvent(new Response(ResponseStatus.M_UPDATE_CONVERSATION, Response.MESSAGE), "12345678");
        String str = JSON.toJSONString(e);
        System.out.println(str);
        ResponseEvent e1 = JSON.parseObject(str, ResponseEvent.class);
        System.out.println(e1);

//        FileInputStream in = new FileInputStream();
    }
}
