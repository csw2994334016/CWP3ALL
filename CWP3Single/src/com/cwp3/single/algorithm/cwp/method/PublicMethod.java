package com.cwp3.single.algorithm.cwp.method;

import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.cwp.modal.CWPBay;
import com.cwp3.single.algorithm.cwp.modal.CWPCrane;
import com.cwp3.single.algorithm.cwp.modal.DPPair;
import com.cwp3.single.algorithm.cwp.modal.DPResult;
import com.cwp3.single.data.CwpData;
import com.cwp3.utils.CalculateUtil;

import java.util.*;

/**
 * Created by csw on 2017/9/19.
 * Description:
 */
public class PublicMethod {

    public static void sortCwpBayByWorkPosition(List<CWPBay> cwpBayList) {
        Collections.sort(cwpBayList, new Comparator<CWPBay>() {
            @Override
            public int compare(CWPBay o1, CWPBay o2) {
                return o1.getWorkPosition().compareTo(o2.getWorkPosition());
            }
        });
    }

    public static void sortCwpCraneByCraneSeq(List<CWPCrane> cwpCraneList) {
        Collections.sort(cwpCraneList, new Comparator<CWPCrane>() {
            @Override
            public int compare(CWPCrane o1, CWPCrane o2) {
                if (o1.getCraneSeq() != null && o2.getCraneSeq() != null) {
                    return o1.getCraneSeq().compareTo(o2.getCraneSeq());
                } else {
                    if (o1.getDpCurrentWorkPosition().equals(o2.getDpCurrentWorkPosition())) {
                        return o1.getCraneNo().compareTo(o2.getCraneNo());
                    } else {
                        return o1.getDpCurrentWorkPosition().compareTo(o2.getDpCurrentWorkPosition());
                    }
                }
            }
        });
    }

    public static void sortWorkMoveListByPlanStartTime(List<WorkMove> workMoveList) {
        Collections.sort(workMoveList, new Comparator<WorkMove>() {
            @Override
            public int compare(WorkMove o1, WorkMove o2) {
                return o1.getPlanStartTime().compareTo(o2.getPlanStartTime());
            }
        });
    }

    public static void sortWorkMoveListByMoveOrder(List<WorkMove> workMoveList) {
        Collections.sort(workMoveList, new Comparator<WorkMove>() {
            @Override
            public int compare(WorkMove o1, WorkMove o2) {
                return o1.getMoveOrder().compareTo(o2.getMoveOrder());
            }
        });
    }

    public static void sortCwpBayByWorkTimeDesc(List<CWPBay> cwpBayList) {
        Collections.sort(cwpBayList, new Comparator<CWPBay>() {
            @Override
            public int compare(CWPBay o1, CWPBay o2) {
                return o2.getDpCurrentTotalWorkTime().compareTo(o1.getDpCurrentTotalWorkTime());
            }
        });
    }

    public static long getCurTotalWorkTime(List<CWPBay> cwpBays) {
        long wt = 0;
        for (CWPBay cwpBay : cwpBays) {
            wt += cwpBay.getDpCurrentTotalWorkTime();
        }
        return wt;
    }

    public static long getCurTotalWorkTimeWithHatchScanTime(List<CWPBay> cwpBays, long hatchScanTime) {
        long wt = 0;
        for (CWPBay cwpBay : cwpBays) {
            wt += cwpBay.getDpCurrentTotalWorkTime();
            if (cwpBay.getDpCurrentTotalWorkTime() > 0) {
                wt += hatchScanTime;
            }
        }
        return wt;
    }


    public static int getMaxCraneNum(List<CWPBay> cwpBays, CwpData cwpData) {
        double craneSafeSpan = 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance();
        int maxCraneNum = 0;
        for (int j = 0; j < cwpBays.size(); ) {
            CWPBay cwpBayJ = cwpBays.get(j);
            if (cwpBayJ.getDpCurrentTotalWorkTime() > 0) {
                int k = j;
                for (; k < cwpBays.size(); k++) {
                    CWPBay cwpBayK = cwpBays.get(k);
                    double distance = CalculateUtil.sub(cwpBayK.getWorkPosition(), cwpBayJ.getWorkPosition());
                    if (distance >= craneSafeSpan) {
                        break;
                    }
                }
                j = k;
                maxCraneNum++;
            } else {
                j++;
            }
        }
        return maxCraneNum;
    }

    public static Integer getSelectBayNoInDpResult(String craneNo, DPResult dpResult) {
        for (DPPair dpPair : dpResult.getDpTraceBack()) {
            String craneNo1 = (String) dpPair.getFirst();
            if (craneNo1.equals(craneNo)) {
                return (Integer) dpPair.getSecond();
            }
        }
        return null;
    }

    public static String getSelectCraneNoInDpResult(Integer bayNo, DPResult dpResult) {
        for (DPPair dpPair : dpResult.getDpTraceBack()) {
            Integer bayNo1 = (Integer) dpPair.getSecond();
            if (bayNo1.equals(bayNo)) {
                return (String) dpPair.getFirst();
            }
        }
        return null;
    }

    public static Integer getCurBayNoInCranePosition(String craneNo, List<DPPair> dpCranePosition) {
        for (DPPair dpPair : dpCranePosition) {
            String craneNo1 = (String) dpPair.getFirst();
            if (craneNo1.equals(craneNo)) {
                return (Integer) dpPair.getSecond();
            }
        }
        return null;
    }


    public static String safeSpanBayBySelected(CWPBay cwpBay, CwpData cwpData) {
        List<DPPair> dpTraceBack = cwpData.getDpResult().getDpTraceBack();
        for (DPPair dpPair : dpTraceBack) {
            CWPBay cwpBay1 = cwpData.getCWPBayByBayNo((Integer) dpPair.getSecond());
            if (safeSpanBay(cwpBay, cwpBay1, cwpData.getWorkingData().getCwpConfig().getSafeDistance())) {
                return (String) dpPair.getFirst();
            }
        }
        return null;
    }

    private static boolean safeSpanBay(CWPBay cwpBay, CWPBay cwpBay1, Double safeSpan) {
        return Math.abs(CalculateUtil.sub(cwpBay.getWorkPosition(), cwpBay1.getWorkPosition())) < 2 * safeSpan;
    }

    public static boolean safeSpanBay(Integer bayNo, Integer bayNo1, CwpData cwpData) {
        return safeSpanBay(cwpData.getCWPBayByBayNo(bayNo), cwpData.getCWPBayByBayNo(bayNo1), cwpData.getWorkingData().getCwpConfig().getSafeDistance());
    }

    public static boolean selectBaysIncludeMachine(CWPCrane cwpCrane, CwpData cwpData) {
        boolean include = false;
        if (cwpCrane.getDpCurCanSelectBays().size() > 0) {
            CWPBay cwpBayL = cwpData.getCWPBayByBayNo(cwpCrane.getDpCurCanSelectBays().getFirst());
            CWPBay cwpBayR = cwpData.getCWPBayByBayNo(cwpCrane.getDpCurCanSelectBays().getLast());
            for (CWPBay cwpMachine : cwpData.getAllMachineBays()) {
                if (cwpMachine.getWorkPosition() > cwpBayL.getWorkPosition() && cwpMachine.getWorkPosition() < cwpBayR.getWorkPosition()) {
                    include = true;
                }
            }
        }
        return include;
    }

    public static long getCraneCurAllWorkTime(CWPCrane cwpCrane, CwpData cwpData) {
        long wt = 0;
        for (Integer bayNo : cwpCrane.getDpCurCanSelectBays()) {
            wt += cwpData.getCWPBayByBayNo(bayNo).getDpCurrentTotalWorkTime();
        }
        return wt;
    }

    public static CWPBay getNextBay(CWPBay cwpBay, CwpData cwpData) {
        List<CWPBay> cwpBayList = cwpData.getAllCWPBays();
        for (int j = 0; j < cwpBayList.size(); j++) {
            if (cwpBayList.get(j).getBayNo().equals(cwpBay.getBayNo())) {
                if (j + 1 < cwpBayList.size()) {
                    return cwpBayList.get(j + 1);
                }
            }
        }
        return cwpBay;
    }

    private static CWPBay getFrontBay(CWPBay cwpBay, CwpData cwpData) {
        List<CWPBay> cwpBayList = cwpData.getAllCWPBays();
        for (int j = 0; j < cwpBayList.size(); j++) {
            if (cwpBayList.get(j).getBayNo().equals(cwpBay.getBayNo())) {
                if (j - 1 >= 0) {
                    return cwpBayList.get(j - 1);
                }
            }
        }
        return cwpBay;
    }

    public static CWPBay getSideBay(CWPBay cwpBay, CwpData cwpData, String side) {
        if (CWPDomain.L.equals(side)) {
            return getNextBay(cwpBay, cwpData);
        } else {
            return getFrontBay(cwpBay, cwpData);
        }
    }

    public static Map<Integer, List<CWPBay>> getCurEveryRoadBayMap(List<CWPBay> cwpBays, CwpData cwpData) {
        double craneSafeSpan = 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance();
        Map<Integer, List<CWPBay>> everyRoadBayMap = new LinkedHashMap<>();
        for (int j = 0; j < cwpBays.size(); ) {
            CWPBay cwpBayJ = cwpBays.get(j);
            if (cwpBayJ.getDpCurrentTotalWorkTime() > 0) {
                int k = j;
                everyRoadBayMap.put(cwpBayJ.getBayNo(), new ArrayList<CWPBay>());
                for (; k < cwpBays.size(); k++) {
                    CWPBay cwpBayK = cwpBays.get(k);
                    double distance = CalculateUtil.sub(cwpBayK.getWorkPosition(), cwpBayJ.getWorkPosition());
                    if (distance >= craneSafeSpan) {
                        break;
                    }
                    if (cwpBayK.getDpCurrentTotalWorkTime() > 0) {
                        everyRoadBayMap.get(cwpBayJ.getBayNo()).add(cwpBayK);
                    }
                }
                j = k;
            } else {
                j++;
            }
        }
        return everyRoadBayMap;
    }

    public static Boolean craneThroughMachine(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        boolean throughMachine = false;
        for (CWPBay cwpBay1 : cwpData.getAllMachineBays()) {
            double machinePo = cwpBay1.getWorkPosition();
            if ((machinePo > cwpBay.getWorkPosition() && machinePo < cwpCrane.getDpCurrentWorkPosition())
                    || (machinePo > cwpCrane.getDpCurrentWorkPosition() && machinePo < cwpBay.getWorkPosition())) {
                throughMachine = true;
                break;
            }
        }
        return throughMachine;
    }

    public static List<CWPCrane> copyCwpCraneList(List<CWPCrane> dpCraneList) {
        List<CWPCrane> cwpCraneList = new ArrayList<>();
        for (CWPCrane cwpCrane : dpCraneList) {
            cwpCraneList.add(cwpCrane.deepCopy());
        }
        return cwpCraneList;
    }

    public static List<CWPBay> getMaxWorkTimeCWPBayList(List<CWPBay> cwpBays, CwpData cwpData) {
        double craneSafeSpan = 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance();
        long maxWorkTime = Long.MIN_VALUE;
        List<CWPBay> maxCwpBayList = new ArrayList<>();
        for (int j = 0; j < cwpBays.size(); j++) {
            CWPBay cwpBayJ = cwpBays.get(j);
            if (cwpBayJ.getDpCurrentTotalWorkTime() > 0) {
                int k = j;
                Long tempWorkTime = 0L;
                List<CWPBay> tempCwpBayList = new ArrayList<>();
                for (; k < cwpBays.size(); k++) {
                    CWPBay cwpBayK = cwpBays.get(k);
                    double distance = CalculateUtil.sub(cwpBayK.getWorkPosition(), cwpBayJ.getWorkPosition());
                    if (distance < craneSafeSpan) {
                        if (cwpBayK.getDpCurrentTotalWorkTime() > 0) {
                            tempWorkTime += cwpBayK.getDpCurrentTotalWorkTime();
                            tempCwpBayList.add(cwpBayK);
                        }
                    } else {
                        if (tempWorkTime > maxWorkTime) {
                            maxWorkTime = tempWorkTime;
                            maxCwpBayList.clear();
                            maxCwpBayList.addAll(tempCwpBayList);
                        }
                        break;
                    }
                }
            }
        }
        return maxCwpBayList;
    }

    public static List<CWPBay> getSideCwpBayList(String side, List<CWPBay> maxCwpBayList, List<CWPBay> cwpBays) {
        List<CWPBay> sideCwpBayList = new ArrayList<>();
        if (maxCwpBayList.isEmpty() || cwpBays.isEmpty()) {
            return sideCwpBayList;
        }
        sortCwpBayByWorkPosition(maxCwpBayList);
        for (CWPBay cwpBay : cwpBays) {
            if (side.equals(CWPDomain.L)) {
                if (cwpBay.getWorkPosition().compareTo(maxCwpBayList.get(0).getWorkPosition()) < 0) {
                    sideCwpBayList.add(cwpBay);
                }
            }
            if (side.equals(CWPDomain.R)) {
                if (cwpBay.getWorkPosition().compareTo(maxCwpBayList.get(maxCwpBayList.size() - 1).getWorkPosition()) > 0) {
                    sideCwpBayList.add(cwpBay);
                }
            }
        }
        return sideCwpBayList;
    }

    public static CWPCrane getFrontCrane(CWPCrane cwpCrane, List<CWPCrane> cwpCraneList) {
        for (int i = 0; i < cwpCraneList.size(); i++) {
            if (cwpCraneList.get(i).getCraneNo().equals(cwpCrane.getCraneNo())) {
                if (i - 1 >= 0) {
                    return cwpCraneList.get(i - 1);
                }
            }
        }
        return null;
    }

    public static CWPCrane getNextCrane(CWPCrane cwpCrane, List<CWPCrane> cwpCraneList) {
        for (int i = 0; i < cwpCraneList.size(); i++) {
            if (cwpCraneList.get(i).getCraneNo().equals(cwpCrane.getCraneNo())) {
                if (i + 1 < cwpCraneList.size()) {
                    return cwpCraneList.get(i + 1);
                }
            }
        }
        return null;
    }

    public static List<DPPair> copyDpPairList(List<DPPair> dpPairList) {
        List<DPPair> dpPairList1 = new ArrayList<>();
        for (DPPair dpPair : dpPairList) {
            DPPair dpPair1 = new DPPair<>(dpPair.getFirst(), dpPair.getSecond());
            dpPairList1.add(dpPair1);
        }
        return dpPairList1;
    }

    public static boolean inDpPairList(DPPair dpPair, List<DPPair> dpPairList) {
        for (DPPair dpPair1 : dpPairList) {
            if (dpPair.getFirst().equals(dpPair1.getFirst()) && dpPair.getSecond().equals(dpPair1.getSecond())) {
                return true;
            }
        }
        return false;
    }

    public static CWPCrane getCwpCraneByCraneNo(String craneNo, List<CWPCrane> dpCwpCraneList) {
        for (CWPCrane cwpCrane : dpCwpCraneList) {
            if (cwpCrane.getCraneNo().equals(craneNo)) {
                return cwpCrane;
            }
        }
        return null;
    }

    public static String getSideByLittleBay(CWPBay cwpBay, CwpData cwpData) {
        CWPBay cwpBayD = cwpData.getCWPBayByBayNo(cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNoD());
        if (cwpBay.getWorkPosition().compareTo(cwpBayD.getWorkPosition()) < 0) {
            return CWPDomain.L;
        }
        if (cwpBay.getWorkPosition().compareTo(cwpBayD.getWorkPosition()) > 0) {
            return CWPDomain.R;
        }
        return null;
    }

    public static List<CWPBay> getSideCwpBayListInSafeSpan(String side, CWPBay cwpBay, CwpData cwpData) {
        double craneSafeSpan = 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance();
        List<CWPBay> cwpBayList = new ArrayList<>();
        if (cwpBay == null || side == null) {
            return cwpBayList;
        }
        List<CWPBay> cwpBays = cwpData.getAllCWPBays();
        for (CWPBay cwpBay1 : cwpBays) {
            if (cwpBay1.getDpCurrentTotalWorkTime() > 0) {
                double distance = CalculateUtil.sub(cwpBay1.getWorkPosition(), cwpBay.getWorkPosition());
                if (side.equals(CWPDomain.R)) {
                    if (distance >= 0 && distance < craneSafeSpan) {
                        cwpBayList.add(cwpBay1);
                    }
                } else if (side.equals(CWPDomain.L)) {
                    if (distance <= 0 && distance > -craneSafeSpan) {
                        cwpBayList.add(cwpBay1);
                    }
                }
            }
        }
        return cwpBayList;
    }

    public static CWPCrane getSideCwpCrane(String side, CWPCrane cwpCrane, List<CWPCrane> cwpCraneList) {
        if (CWPDomain.L.equals(side)) {
            return getFrontCrane(cwpCrane, cwpCraneList);
        }
        if (CWPDomain.R.equals(side)) {
            return getNextCrane(cwpCrane, cwpCraneList);
        }
        return null;
    }

    public static List<CWPCrane> getSideCwpCraneList(String side, CWPCrane cwpCrane, List<CWPCrane> cwpCraneList) {
        List<CWPCrane> cwpCraneList1 = new ArrayList<>();
        for (CWPCrane cwpCrane1 : cwpCraneList) {
            if (CWPDomain.L.equals(side)) {
                if (cwpCrane1.getCraneSeq().compareTo(cwpCrane.getCraneSeq()) < 0) {
                    cwpCraneList1.add(cwpCrane1);
                }
            }
            if (CWPDomain.R.equals(side)) {
                if (cwpCrane1.getCraneSeq().compareTo(cwpCrane.getCraneSeq()) > 0) {
                    cwpCraneList1.add(cwpCrane1);
                }
            }
        }
        return cwpCraneList1;
    }

    public static boolean inCwpBayList(CWPBay cwpBay1, List<CWPBay> cwpBayList) {
        for (CWPBay cwpBay : cwpBayList) {
            if (cwpBay.getBayNo().equals(cwpBay1.getBayNo())) {
                return true;
            }
        }
        return false;
    }

    public static CWPBay getSteppingBayInHatch(CWPBay cwpBay, CwpData cwpData, String side) {
        if (cwpBay.getBayNo() % 2 == 0) {
            CWPBay cwpBay1 = cwpData.getCWPBayByBayNo(cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNo1());
            CWPBay cwpBay2 = cwpData.getCWPBayByBayNo(cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNo2());
            if (CWPDomain.L.equals(side)) {
                return cwpBay1.getWorkPosition().compareTo(cwpBay.getWorkPosition()) < 0 ? cwpBay1 : cwpBay2;
            } else if (CWPDomain.R.equals(side)) {
                return cwpBay1.getWorkPosition().compareTo(cwpBay.getWorkPosition()) > 0 ? cwpBay1 : cwpBay2;
            }
        }
        return null;
    }

    public static Map<Integer, List<CWPBay>> getCwpHatchBayMap(List<CWPBay> cwpBays, CwpData cwpData) {
        Map<Integer, List<CWPBay>> hatchBayMap = new LinkedHashMap<>();
        Integer bayNoD;
        for (CWPBay cwpBay : cwpBays) {
            if (cwpBay.getDpCurrentTotalWorkTime() > 0) {
                bayNoD = cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNoD();
                if (hatchBayMap.get(bayNoD) == null) {
                    hatchBayMap.put(bayNoD, new ArrayList<CWPBay>());
                }
                hatchBayMap.get(bayNoD).add(cwpBay);
            }
        }
        return hatchBayMap;
    }

    public static Map<Integer, List<CWPBay>> getBayRangeMapByMachine(Map<Integer, List<CWPBay>> hatchBayMap, CwpData cwpData) {
        List<CWPBay> machines = cwpData.getAllMachineBays();
        Map<Integer, List<CWPBay>> bayRangeMap = new HashMap<>();
        for (Map.Entry<Integer, List<CWPBay>> entry : hatchBayMap.entrySet()) {
            CWPBay cwpBayD = cwpData.getCWPBayByBayNo(entry.getKey());
            if (machines.size() == 1) {
                if (cwpBayD.getWorkPosition().compareTo(machines.get(0).getWorkPosition()) < 0) {
                    if (bayRangeMap.get(0) == null) {
                        bayRangeMap.put(0, new ArrayList<CWPBay>());
                    }
                    bayRangeMap.get(0).add(cwpBayD);
                } else {
                    if (bayRangeMap.get(1) == null) {
                        bayRangeMap.put(1, new ArrayList<CWPBay>());
                    }
                    bayRangeMap.get(1).add(cwpBayD);
                }
            } else if (machines.size() == 2) {
                if (cwpBayD.getWorkPosition().compareTo(machines.get(0).getWorkPosition()) < 0) {
                    if (bayRangeMap.get(0) == null) {
                        bayRangeMap.put(0, new ArrayList<CWPBay>());
                    }
                    bayRangeMap.get(0).add(cwpBayD);
                } else if ((cwpBayD.getWorkPosition().compareTo(machines.get(0).getWorkPosition()) > 0) &&
                        (cwpBayD.getWorkPosition().compareTo(machines.get(1).getWorkPosition()) < 0)) {
                    if (bayRangeMap.get(1) == null) {
                        bayRangeMap.put(1, new ArrayList<CWPBay>());
                    }
                    bayRangeMap.get(1).add(cwpBayD);
                } else {
                    if (bayRangeMap.get(2) == null) {
                        bayRangeMap.put(2, new ArrayList<CWPBay>());
                    }
                    bayRangeMap.get(2).add(cwpBayD);
                }
            } else {
                if (bayRangeMap.get(0) == null) {
                    bayRangeMap.put(0, new ArrayList<CWPBay>());
                }
                bayRangeMap.get(0).add(cwpBayD);
            }
        }
        return bayRangeMap;
    }

    public static Map<Integer, List<CWPBay>> getBayRangeMapByMachine(CwpData cwpData) {
        List<CWPBay> machines = cwpData.getAllMachineBays();
        Map<Integer, List<CWPBay>> bayRangeMap = new HashMap<>();
        for (Map.Entry<Integer, List<CWPBay>> entry : cwpData.getCwpHatchBayMap().entrySet()) {
            CWPBay cwpBayD = cwpData.getCWPBayByBayNo(entry.getKey());
            if (machines.size() == 1) {
                if (cwpBayD.getWorkPosition().compareTo(machines.get(0).getWorkPosition()) < 0) {
                    if (bayRangeMap.get(0) == null) {
                        bayRangeMap.put(0, new ArrayList<CWPBay>());
                    }
                    bayRangeMap.get(0).addAll(entry.getValue());
                } else {
                    if (bayRangeMap.get(1) == null) {
                        bayRangeMap.put(1, new ArrayList<CWPBay>());
                    }
                    bayRangeMap.get(1).addAll(entry.getValue());
                }
            } else if (machines.size() == 2) {
                if (cwpBayD.getWorkPosition().compareTo(machines.get(0).getWorkPosition()) < 0) {
                    if (bayRangeMap.get(0) == null) {
                        bayRangeMap.put(0, new ArrayList<CWPBay>());
                    }
                    bayRangeMap.get(0).addAll(entry.getValue());
                } else if ((cwpBayD.getWorkPosition().compareTo(machines.get(0).getWorkPosition()) > 0) &&
                        (cwpBayD.getWorkPosition().compareTo(machines.get(1).getWorkPosition()) < 0)) {
                    if (bayRangeMap.get(1) == null) {
                        bayRangeMap.put(1, new ArrayList<CWPBay>());
                    }
                    bayRangeMap.get(1).addAll(entry.getValue());
                } else {
                    if (bayRangeMap.get(2) == null) {
                        bayRangeMap.put(2, new ArrayList<CWPBay>());
                    }
                    bayRangeMap.get(2).addAll(entry.getValue());
                }
            } else {
                if (bayRangeMap.get(0) == null) {
                    bayRangeMap.put(0, new ArrayList<CWPBay>());
                }
                bayRangeMap.get(0).addAll(entry.getValue());
            }
        }
        return bayRangeMap;
    }

    public static List<CWPBay> getCwpBayListToCwpBayListD(List<CWPBay> cwpBays, CwpData cwpData) {
        List<CWPBay> cwpBayListD = new ArrayList<>();
        Set<Integer> bayNoDSet = new LinkedHashSet<>();
        for (CWPBay cwpBay : cwpBays) {
            if (cwpBay.getDpCurrentTotalWorkTime() > 0) {
                Integer bayNoD = cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNoD();
                bayNoDSet.add(bayNoD);
            }
        }
        for (Integer bayNoD : bayNoDSet) {
            cwpBayListD.add(cwpData.getCWPBayByBayNo(bayNoD));
        }
        return cwpBayListD;
    }

    public static List<CWPBay> getCwpBayListByWtNotZero(List<CWPBay> cwpBays) {
        List<CWPBay> cwpBayList = new LinkedList<>();
        for (CWPBay cwpBay : cwpBays) {
            if (cwpBay.getDpCurrentTotalWorkTime() > 0) {
                cwpBayList.add(cwpBay);
            }
        }
        return cwpBayList;
    }

    public static String getCraneNoStr(List<CWPCrane> cwpCranes) {
        StringBuilder craneNoStr = new StringBuilder();
        for (CWPCrane craneNo : cwpCranes) {
            craneNoStr.append(craneNo.getCraneNo()).append(" ");
        }
        return craneNoStr.toString();
    }

    public static String getBayNoStr(List<CWPBay> cwpBays) {
        StringBuilder bayNoStr = new StringBuilder();
        for (CWPBay cwpBay : cwpBays) {
            bayNoStr.append(cwpBay.getBayNo()).append(" ");
        }
        return bayNoStr.toString();
    }

    public static int getAvailableCwpBayNum(List<CWPBay> cwpBays) {
        int n = 0;
        for (CWPBay cwpBay : cwpBays) {
            n = cwpBay.getDpCurrentTotalWorkTime() > 0 ? n + 1 : n;
        }
        return n;
    }

    public static boolean isLittleBay(CWPBay cwpBay, CwpData cwpData) {
        return cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNos().size() == 2 && cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNos().contains(cwpBay.getBayNo());
    }

    public static boolean machineBetweenBay(List<Integer> bayNos, CwpData cwpData) {
        for (int i = 0; i < bayNos.size(); i++) {
            CWPBay cwpBay1 = cwpData.getCWPBayByBayNo(bayNos.get(i));
            if (i + 1 < bayNos.size()) {
                CWPBay cwpBay2 = cwpData.getCWPBayByBayNo(bayNos.get(i + 1));
                List<CWPBay> machineBayList = cwpData.getAllMachineBays();
                for (CWPBay cwpBay : machineBayList) {
                    if (cwpBay1.getWorkPosition().compareTo(cwpBay2.getWorkPosition()) < 0) {
                        if (cwpBay.getWorkPosition().compareTo(cwpBay1.getWorkPosition()) > 0 && cwpBay2.getWorkPosition().compareTo(cwpBay.getWorkPosition()) > 0) {
                            return true;
                        }
                    } else if (cwpBay1.getWorkPosition().compareTo(cwpBay2.getWorkPosition()) > 0){
                        if (cwpBay.getWorkPosition().compareTo(cwpBay2.getWorkPosition()) > 0 && cwpBay1.getWorkPosition().compareTo(cwpBay.getWorkPosition()) > 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
