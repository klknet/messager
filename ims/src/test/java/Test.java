import org.apache.commons.lang3.StringUtils;

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
//        System.out.println(1L<<64);
//        System.out.println(1<<31);
//        System.out.println(1>>2);
//        System.out.println(-2%16);

        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse("2019-03-26T11:42:19.344+0000");
            System.out.println(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String status = "https://testdevcdn.xylink.com/download100165977_1557732374007.xls";
        System.out.println(status.lastIndexOf("_"));
        System.out.println(status.lastIndexOf("."));
//        String d = status.substring(status.lastIndexOf("_"), status.lastIndexOf("\\."));
//        System.out.println(d);

        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        List<Integer> list2 = list.stream().filter(i -> i < 3).collect(Collectors.toList());
        System.out.println(list);
        System.out.println(list2);
        String s = "阿里巴巴Java开发手册.pdf";
        System.out.println(s.substring(s.lastIndexOf(".")));
        System.out.println( StringUtils.substringAfter(s, "."));
    }
}
