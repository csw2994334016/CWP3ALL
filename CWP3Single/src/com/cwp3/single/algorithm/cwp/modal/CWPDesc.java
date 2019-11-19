package com.cwp3.single.algorithm.cwp.modal;

/**
 * Created by csw on 2018/6/27.
 * Description:
 */
public enum CWPDesc {

    canNotWork(0, "不能选择该倍位（物理移动限制/没有可作业量）"),
    outWorkRange(1, "桥机平均作业量划分范围之外"),
    reStowCntDelay(1, "该倍位出翻舱箱子还没有卸船，则需要推迟作业"),
    nonLockBayCrane(1, "该倍位被锁定，则其它桥机不能选择"),

    inWorkRange(2, "桥机平均作业量划分范围之内"),
    hatchBayDelay(1, "同一个舱内倍位推迟作业更合适"),
    hatchBayFirst(3, "同一个舱内倍位优先作业更合适"),

    firstSelectFactor(4, "第一次决策"),

    splitRoad(3, "劈路作业原则"),
    specialBay(3, "尽量让桥机做完驾驶台/烟囱同边的倍位"),

    preAvoidKeyRoad(4, "预判该倍位如果不被选择作业，会形成重点路"),
    keepFirstCraneWork(4, "保持第一部桥机继续作业"),

    lastSelectHatch(4, "桥机保持在上次选择的舱中作业"),
    steppingCntFirst(5, "同一个舱内垫脚箱可以连续做完时，优先选择垫脚作业"),

    mustSelectByCrane(6, "该倍位必须由该桥机作业（避免形成新的重点路）"),

    bayLockByCrane(7, "人工锁定桥机作业倍位，优先级最高");

    private int code;
    private String desc;

    CWPDesc(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
