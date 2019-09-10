package com.cwp3.multiple.model;

import com.cwp3.model.crane.CMCrane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2018/11/12.
 * Description:
 */
public class Crane {

    private CMCrane cmCrane; // 桥机基础信息

    private Map<Long, Long> timeBerthMap; // 桥机在每个时间点被哪条船使用

    public Crane(CMCrane cmCrane) {
        this.cmCrane = cmCrane;
        timeBerthMap = new HashMap<>();
    }

    public CMCrane getCmCrane() {
        return cmCrane;
    }

    public String getCraneNo() {
        return cmCrane.getCraneNo();
    }

}
