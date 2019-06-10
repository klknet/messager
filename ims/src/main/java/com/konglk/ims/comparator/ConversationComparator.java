package com.konglk.ims.comparator;

import com.konglk.ims.domain.ConversationDO;

import java.util.Comparator;

/**
 * Created by konglk on 2019/6/7.
 */
public class ConversationComparator  {

    /*
    根据置顶时间倒序排列
     */
    public static Comparator<ConversationDO> compareUpdateTime() {
        return (c1, c2) -> {
            if (c1.getTopUpdateTime() == null && c2.getTopUpdateTime() == null)
                return 0;
            if (c1.getTopUpdateTime() != null && c2.getTopUpdateTime() == null)
                return -1;
            if (c1.getTopUpdateTime() == null && c2.getTopUpdateTime() != null)
                return 1;
            return c2.getTopUpdateTime().compareTo(c1.getTopUpdateTime());
        };
    }


}
