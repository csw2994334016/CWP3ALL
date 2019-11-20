package com.cwp3.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Created by csw on 2017/4/24 15:58.
 * Explain:
 */
public class CWPDefaultValue {

    public static final String CWP_VERSION = "CWP3.0.19.11.20";
    public static final String CRANE_ALLOCATION_VERSION = "CRANE_ALLOCATION.2018.11.29";
    public static final String MULTIPLE_CWP_VERSION = "MULTIPLE_CWP.2019.8.12";
    public static boolean outputLogToConsole = true;

    public static Integer twinWeightDiff = 100000; //重量差100吨，即默认没有重量差

    public static Long oneCntWorkTime = 144L;
    public static Long keyBayWorkTime = 6 * 3600L; //21600
    public static Long dividedBayWorkTime = 2 * 3600L;
    public static Long keepSelectedBayWorkTime = 10 * 3600L;
    public static Double machineHeight = 15.0;
    public static Boolean crossBridge = true;
    public static Boolean crossChimney = true;
    public static Long crossBarTime = 900L;
    public static Double craneSafeSpan = 14.0;
    public static Double craneSpeed = 0.75;//m/s
    public static Long delCraneTimeParam = 1800L;
    public static Long addOrDelCraneTime = 1800L;
    public static Long amount = 15L;
    public static Long breakDownCntTime = 30 * 60L;

    public static Boolean keyBay = true; //大船的情况重点路权重对结果没什么影响
    public static Boolean divideByMaxRoad = false;

    public static List<String> gpCntTypes = Arrays.asList("GP", "HC", "GP,HC", "BU", "BV"); //普通箱定义，可以做双箱吊

    public static Long hatchCoverOpenTime = 240L;
    public static Long hatchCoverCloseTime = 240L;
    public static Long unlockTwistTime = 90L;
    public static Long single20Time = 120L;
    public static Long single20FootPadTime = 180L;
    public static Long single20SeparateTime = 180L;
    public static Long single40Time = 120L;
    public static Long single45Time = 120L;
    public static Long double20Time = 150L;
    public static Long double40Time = 140L;
    public static Long double45Time = 140L;
    public static Long specialCntTime = 360L;
    public static Long dangerCntTime = 360L;
    public static Double impactFactor = 1.0D;
    public static Long hatchScanTime = 300L;
    public static String craneSameWorkTime = "Y";
    public static String mainRoadOneCrane = "N";
    public static String craneNotSameWorkTime = "N";
    public static String dividedHatchFirst = "N";
    public static String deckWorkLater = "N";
    public static String splitRoad = "Y";
    public static String recycleCntWiFlag = "N";


    // 多船默认参数
    public static Long one_2_D_CntTime = 144L;
    public static Long one_2_L_CntTime = 144L;
    public static Long one_4_D_CntTime = 144L;
    public static Long one_4_L_CntTime = 144L;
    public static long defaultCntWorkTime = 360;

    public static long steppingCntMoveNum = 10; // 垫脚箱作业旁边桥机等待的关数阈值
    public static long moreCntMoveNum = 2; // 当决策时，发现某大倍位可作业量比决策时间大2关时间时，可以勉强延长决策时间

    public static Double allContainerWeight = 60000D; // 双吊具工艺作业的集装箱合计重量限制，默认<=60000kg可以作业
    public static Double tandemWeightDiff = 20000D; // 双吊具工艺一关作业的两箱重量差限制，默认<=20000kg可作业
    public static Double tandemHeightDiff = 40D; // 双吊具工艺一关作业的两箱高度差限制，默认<=40cm可作业
    public static String tandemContainerType = "GP,HC,OT"; // 双吊具工艺可以作业的箱型，默认值："GP,HC,OT"。（OT箱带超限标记的不包括在内）
    public static String tandemDangerCnt = "3,6,8,9"; // 双吊具工艺可以作业危险品箱型类别，默认值："3,6,8,9"
    public static String multiThreadFlag = "Y";
}
