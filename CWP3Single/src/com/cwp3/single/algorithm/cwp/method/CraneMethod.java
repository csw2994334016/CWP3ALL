package com.cwp3.single.algorithm.cwp.method;

import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.domain.CWPDomain;
import com.cwp3.single.algorithm.cwp.modal.*;
import com.cwp3.single.data.CwpData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by csw on 2018/5/30.
 * Description:
 */
public class CraneMethod {

    public static boolean hasCraneAddOrDelete(List<CWPCrane> allCWPCranes) {
        for (CWPCrane cwpCrane : allCWPCranes) {
            if (cwpCrane.getCwpCraneWorkList().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public static List<CWPCrane> getFirstAvailableCraneList(CwpData cwpData) {
        List<CWPCrane> cwpCraneList = new ArrayList<>();
        for (CWPCrane cwpCrane : cwpData.getAllCWPCranes()) { // 所有桥机
            List<CWPCraneWork> cwpCraneWorkList = cwpCrane.getCwpCraneWorkList();
            if (cwpCraneWorkList.size() > 0) {
                CWPCraneWork cwpCraneWork1 = cwpCraneWorkList.get(0);
                if (CWPDomain.ADD_CRANE.equals(cwpCraneWork1.getAddOrDeleteFlag())) { // 判断上路时间点是否在当前时间范围之内
                    long curTime = cwpData.getDpCurrentTime();
                    long addOrDelTime = cwpCraneWork1.getAddOrDeleteTime().getTime() / 1000;
                    long a_c = addOrDelTime - curTime;
                    if (addOrDelTime > curTime && a_c < CWPDefaultValue.addOrDelCraneTime) {
                        cwpCraneList.add(cwpCrane);
                        cwpCraneWorkList.remove(0);
                    }
                } else {
                    cwpCraneList.add(cwpCrane);
                }
            } else {
                cwpCraneList.add(cwpCrane);
            }
        }
        return cwpCraneList;
    }

    public static List<CWPCrane> getAvailableCraneList(List<CWPCrane> dpCwpCraneList, CwpData cwpData) {
        // 在该时刻可以作业的桥机，考虑加减桥机信息
        List<CWPCrane> cwpCraneList = new ArrayList<>();
        for (CWPCrane cwpCrane : cwpData.getAllCWPCranes()) { // 所有桥机
            CWPCrane availableCrane = PublicMethod.getCwpCraneByCraneNo(cwpCrane.getCraneNo(), dpCwpCraneList); // 已经被选中作业了的桥机
            if (availableCrane != null) {
                List<CWPCraneWork> cwpCraneWorkList = availableCrane.getCwpCraneWorkList();
                if (cwpCraneWorkList.size() > 0) {
                    CWPCraneWork cwpCraneWork1 = cwpCraneWorkList.get(0);
                    if (CWPDomain.DELETE_CRANE.equals(cwpCraneWork1.getAddOrDeleteFlag())) { // 只判断下路时间点是否在当前时间范围之外
                        long curTime = cwpData.getDpCurrentTime();
                        long addOrDelTime = cwpCraneWork1.getAddOrDeleteTime().getTime() / 1000;
                        long a_c = addOrDelTime - curTime;
                        // 如果下路桥机当前作业的倍位还剩余小于半个小时的量，则可以继续多作业半个小时
                        boolean continueLastBayNoFlag = false;
                        Integer bayNoLast = PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult());
                        if (bayNoLast != null) {
                            CWPBay cwpBayLast = cwpData.getCWPBayByBayNo(bayNoLast);
                            if (cwpBayLast.getDpAvailableWorkTime() > 0 && cwpBayLast.getDpAvailableWorkTime() < CWPDefaultValue.addOrDelCraneTime) {
                                continueLastBayNoFlag = true;
                            }
                        }
                        if (continueLastBayNoFlag || a_c > CWPDefaultValue.addOrDelCraneTime) { // 下路时间离当前时间大于半个小时，则继续作业
                            cwpCraneList.add(availableCrane);
                        } else {
                            cwpCraneWorkList.remove(0);
                        }
                    }
                } else {
                    cwpCraneList.add(availableCrane);
                }
            } else {
                availableCrane = cwpCrane.deepCopy();
                List<CWPCraneWork> cwpCraneWorkList = availableCrane.getCwpCraneWorkList();
                if (cwpCraneWorkList.size() > 0) {
                    CWPCraneWork cwpCraneWork1 = cwpCraneWorkList.get(0);
                    if (CWPDomain.ADD_CRANE.equals(cwpCraneWork1.getAddOrDeleteFlag())) { // 只判断上路时间点是否在当前时间范围之内
                        long curTime = cwpData.getDpCurrentTime();
                        long addOrDelTime = cwpCraneWork1.getAddOrDeleteTime().getTime() / 1000;
                        long a_c = addOrDelTime - curTime;
                        a_c = Math.abs(a_c);
                        if (a_c <= CWPDefaultValue.addOrDelCraneTime) {
                            cwpCraneList.add(availableCrane);
                            cwpCraneWorkList.remove(0);
                        }
                    }
                }
            }
        }
        if (cwpCraneList.size() == 0) {
            return dpCwpCraneList;
        }
        return cwpCraneList;
    }

    public static long obtainMinWorkTime(DPResult dpResult, CwpData cwpData) {
        if (dpResult.getDpTraceBack().isEmpty()) {
            return 0;
        }
        long minWorkTime = Long.MAX_VALUE;
        for (DPPair dpPair : dpResult.getDpTraceBack()) {
            CWPCrane cwpCrane = PublicMethod.getCwpCraneByCraneNo((String) dpPair.getFirst(), cwpData.getDpCwpCraneList());
            CWPBay cwpBay = cwpData.getCWPBayByBayNo((Integer) dpPair.getSecond());
            long craneMinWorkTime = cwpBay.getDpAvailableWorkTime();
            //卸和装分开
            if (cwpBay.getDpAvailableDiscWtD() > 0 && cwpBay.getDpAvailableLoadWtD() > 0) {
                craneMinWorkTime = cwpBay.getDpAvailableDiscWtD();
            }
            if (cwpCrane != null) {
                //判断分割倍位
                if (cwpBay.getBayNo().equals(cwpCrane.getDpWorkBayNoFrom())) {
                    if (craneMinWorkTime > cwpCrane.getDpWorkTimeFrom() && cwpCrane.getDpWorkTimeFrom() > cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                        craneMinWorkTime = cwpCrane.getDpWorkTimeFrom();
                    }
                }
                if (cwpBay.getBayNo().equals(cwpCrane.getDpWorkBayNoTo())) {
                    if (craneMinWorkTime > cwpCrane.getDpWorkTimeTo() && cwpCrane.getDpWorkTimeTo() > cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                        craneMinWorkTime = cwpCrane.getDpWorkTimeTo();
                    }
                }
            }
            minWorkTime = Math.min(minWorkTime, craneMinWorkTime);
        }
        // 判断上下路时间点
        for (CWPCrane cwpCrane : cwpData.getAllCWPCranes()) { // 所有桥机
            long addOrDelCraneTime = Long.MAX_VALUE;
            CWPCrane curCrane = PublicMethod.getCwpCraneByCraneNo(cwpCrane.getCraneNo(), cwpData.getDpCwpCraneList()); // 已经被选中作业了的桥机
            if (curCrane == null) { // 一般都是待加入作业的桥机的桥机
                curCrane = cwpCrane;
            }
            List<CWPCraneWork> cwpCraneWorkList = curCrane.getCwpCraneWorkList();
            if (cwpCraneWorkList.size() > 0) {
                CWPCraneWork cwpCraneWork = cwpCraneWorkList.get(0);
                String ad = PublicMethod.getCwpCraneByCraneNo(cwpCrane.getCraneNo(), cwpData.getDpCwpCraneList()) != null ? CWPDomain.DELETE_CRANE : CWPDomain.ADD_CRANE;
                if (ad.equals(cwpCraneWork.getAddOrDeleteFlag())) {
                    long curCwpTime = cwpData.getDpCurrentTime();
                    long addOrDelTime = cwpCraneWork.getAddOrDeleteTime().getTime() / 1000;
                    long workToTime = curCwpTime + minWorkTime;
                    long a_c = addOrDelTime - curCwpTime;
                    long w_a = workToTime - addOrDelTime;
                    if (addOrDelTime >= curCwpTime && addOrDelTime <= workToTime) {
//                        if (a_c <= CWPDefaultValue.addOrDelCraneTime) {
//                            addOrDelCraneTime = 0;
//                        } else if (w_a <= CWPDefaultValue.addOrDelCraneTime) {
//                            addOrDelCraneTime = minWorkTime;
//                        } else {
//                            addOrDelCraneTime = a_c;
//                        }
                        if (w_a <= CWPDefaultValue.addOrDelCraneTime) {
                            addOrDelCraneTime = minWorkTime;
                        } else {
                            addOrDelCraneTime = a_c;
                        }
                    }
                }
            }
            minWorkTime = Math.min(minWorkTime, addOrDelCraneTime);
        }
        return minWorkTime;
    }
}
