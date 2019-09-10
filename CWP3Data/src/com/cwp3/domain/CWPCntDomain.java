package com.cwp3.domain;

/**
 * Created by csw on 2017/4/27 15:03.
 * Explain:
 */
public final class CWPCntDomain {

    //workStatus：箱子作业状态
    /**
     * 发送
     */
    public static final String A = "A";
    /**
     * 作业中
     */
    public static final String W = "W";
    /**
     * 完成
     */
    public static final String C = "C";
    /**
     * 完成
     */
    public static final String RC = "RC";
    /**
     * 未发送，卸船
     */
    public static final String Y = "Y";
    /**
     * 未发送，装船，已经配载
     */
    public static final String S = "S";
    /**
     * 未发送，装船，未配载的指令
     */
    public static final String P = "P";
    /**
     * 退卸或退装
     */
    public static final String R = "R";

    //machineStatus: 作业中的箱子有这个字段，作业这个箱子的机械状态
    /**
     * 所作业的机械故障
     */
    public static final String BREAKDOWN = "BREAKDOWN";
    /**
     * 所作业的机械正常
     */
    public static final String NORMAL = "NORMAL";

    //moveStage: 箱子运动阶段
    /**
     * 集卡
     */
    public static final String TRUCK = "TRUCK";
    /**
     * 箱区
     */
    public static final String YARD = "YARD";
    /**
     * 箱区转接位置
     */
    public static final String YARDTEMP = "YARDTEMP";
    /**
     * 支架
     */
    public static final String RACK = "RACK";
    /**
     * AGV
     */
    public static final String AGV = "AGV";
    /**
     * ASC
     */
    public static final String ASC = "ASC";
    /**
     * 桥吊车道地面
     */
    public static final String LANEGROUND = "LANEGROUND";
    /**
     * 桥吊平台
     */
    public static final String STSPLATFORM = "STSPF";
    /**
     * 桥吊主小车
     */
    public static final String STSMAIN = "STSMAIN";
    /**
     * 桥吊门架小车
     */
    public static final String STSPQRTAL = "STSPT";
    /**
     * 船上
     */
    public static final String VESSEL = "VESSEL";

}
