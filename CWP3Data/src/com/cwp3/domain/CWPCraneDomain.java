package com.cwp3.domain;

/**
 * Created by csw on 2017/5/2 17:03.
 * Explain:
 */
public final class CWPCraneDomain {


    //桥机作业工艺类型

    public static final String CT_SINGLE20 = "S2";
    public static final String CT_DUAL20 = "D2"; //双箱吊
    public static final String CT_QUAD20 = "Q2"; // 20尺4箱吊，双吊具
    public static final String CT_TWIN20 = "T2"; // 20尺2箱吊，双吊具

    public static final String CT_SINGLE40 = "S4";
    public static final String CT_DUAL40 = "D4"; //双吊具

    /**
     * 舱盖板作业工艺
     */
    public static final String CT_HATCH_COVER = "HC";
    /**
     * 正常作业
     */
    public static final String GREEN = "GREEN";

    /**
     * 报警，但能够完成作业
     */
    public static final String YELLOW = "YELLOW";

    /**
     * 无法作业
     */
    public static final String RED = "RED";

    /**
     * 桥机可以移动
     */
    public static final String STANDING = "YES";

}
