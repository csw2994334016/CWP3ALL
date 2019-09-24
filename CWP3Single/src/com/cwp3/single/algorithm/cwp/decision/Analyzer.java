package com.cwp3.single.algorithm.cwp.decision;

import com.cwp3.domain.CWPDefaultValue;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMContainer;
import com.cwp3.model.vessel.VMPosition;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.model.work.WorkBlock;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.cwp.method.CraneMethod;
import com.cwp3.single.algorithm.cwp.method.LogPrintMethod;
import com.cwp3.single.algorithm.cwp.method.PublicMethod;
import com.cwp3.single.algorithm.cwp.modal.CWPBay;
import com.cwp3.single.algorithm.cwp.modal.CWPCrane;
import com.cwp3.single.algorithm.cwp.modal.CWPCraneWork;
import com.cwp3.single.algorithm.cwp.modal.DPPair;
import com.cwp3.single.data.CwpData;
import com.cwp3.utils.CalculateUtil;

import java.util.*;

/**
 * Created by csw on 2018/6/14.
 * Description:
 */
public class Analyzer {

    public void firstAnalyzeCwpBay(CwpData cwpData) {
        analyzeCwpBay(cwpData);
    }

    public void analyzeCwpBay(CwpData cwpData) {
        List<CWPBay> cwpBayList = cwpData.getAllCWPBays();
        for (CWPBay cwpBay : cwpBayList) {
            //总量分析
            long totalWt = 0;
            long steppingCntTotalWt = 0;
            long reStowCntTimeD = 0; // 出翻舱在这个倍位开始作业的时间
            Map<Integer, List<WorkMove>> tolWorkMoveMap = cwpData.getMoveResults().getTolWorkMoveMapByBayNo(cwpBay.getBayNo());
            for (List<WorkMove> workMoveList : tolWorkMoveMap.values()) {
                for (WorkMove workMove : workMoveList) {
                    totalWt += workMove.getWorkTime();
                    if (workMove.getVmSlotSet().size() == 1) { //判断move是垫脚
                        if (cwpData.getStructureData().isSteppingVMSlot(workMove.getOneVMSlot())) {
                            steppingCntTotalWt += workMove.getWorkTime();
                        }
                    }
                    // 判断箱子是否是出翻舱箱子
                    for (VMSlot vmSlot : workMove.getVmSlotSet()) {
                        VMContainer vmContainer = cwpData.getWorkingData().getVMContainerByVMSlot(vmSlot, workMove.getDlType());
                        if (vmContainer != null && CWPDomain.RE_STOW_VY.equals(vmContainer.getReStowType())) {
                            reStowCntTimeD = totalWt;
                            break;
                        }
                    }
                }
            }
            cwpBay.setDpCurrentTotalWorkTime(totalWt);
            if (cwpData.getFirstDoCwp()) {
                cwpBay.setDpTotalWorkTime(totalWt);
            }
            // 设置这个倍位开始作业卸船出翻舱箱子的时间
            cwpBay.setReStowCntTimeD(reStowCntTimeD);
            //可作业量分析
            long availableWt = 0;
            long steppingCntAvailableWt = 0;
            long availableDiscWtD = 0, availableLoadWtD = 0;
            long availableDiscWtX = 0, availableLoadWtX = 0;
            int loadNum = 0, discNum = 0;
            long reStowCntTimeL = 0;
            long time = cwpData.getDpCurrentTime() - (cwpData.getWorkingData().getVmSchedule().getPlanBeginWorkTime().getTime() / 1000);
            Map<Integer, List<WorkMove>> availableWorkMoveMap = cwpData.getMoveResults().getAvailableWorkMoveMapByBayNo(cwpBay.getBayNo());
            F:
            for (List<WorkMove> workMoveList : availableWorkMoveMap.values()) { //分档进行可作业量分析
                for (WorkMove workMove : workMoveList) {
                    if (time < 3600 && CWPDomain.YES.equals(cwpData.getWorkingData().getCwpConfig().getDeckWorkLater())) { // 第一关是装船,且当前时间在开工时间一个小时内,则该倍位不可作业
                        if (CWPDomain.DL_TYPE_LOAD.equals(workMove.getDlType()) && workMove.getTierNo() > 50) {
                            break F;
                        }
                    }
                    availableWt += workMove.getWorkTime();
                    if (workMove.getVmSlotSet().size() == 1 && cwpData.getStructureData().isSteppingVMSlot(workMove.getOneVMSlot())) { //判断move是垫脚
                        steppingCntAvailableWt += workMove.getWorkTime();
                    }
                    if (workMove.getBayNo() % 2 == 0) { // 大倍位上的箱子
                        availableDiscWtD += workMove.getDlType().equals(CWPDomain.DL_TYPE_DISC) ? workMove.getWorkTime() : 0;
                        availableLoadWtD += workMove.getDlType().equals(CWPDomain.DL_TYPE_LOAD) ? workMove.getWorkTime() : 0;
                    } else {
                        availableDiscWtX += CWPDomain.DL_TYPE_DISC.equals(workMove.getDlType()) ? workMove.getWorkTime() : 0;
                        availableLoadWtX += workMove.getDlType().equals(CWPDomain.DL_TYPE_LOAD) ? workMove.getWorkTime() : 0;
                    }
                    if (CWPDomain.MOVE_TYPE_CNT.equals(workMove.getMoveType())) {
                        if (workMove.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) {
                            loadNum++;
                        } else {
                            discNum++;
                        }
                    }
                    // 装船出翻舱箱子一定要在卸船出翻舱箱子作业之后一个小时
                    if (CWPDomain.DL_TYPE_LOAD.equals(workMove.getDlType())) {
                        for (VMSlot vmSlot : workMove.getVmSlotSet()) {
                            VMContainer vmContainer = cwpData.getWorkingData().getVMContainerByVMSlot(vmSlot, workMove.getDlType());
                            if (vmContainer != null && vmContainer.getYardContainerId() != null) {
                                VMContainer vmContainerD = cwpData.getWorkingData().getReStowContainerMapD().get(vmContainer.getYardContainerId());
                                if (vmContainerD != null) {
                                    // 找到卸船出翻舱箱子的move，判断是否已经作业，决定该倍位是否要推迟作业
                                    VMPosition vmPosition = new VMPosition(vmContainerD.getvLocation());
                                    VMSlot vmSlotD;
                                    if (vmPosition.getBayNo() % 2 == 0) {
                                        vmSlotD = cwpData.getStructureData().getVMSlotByVLocation(new VMPosition(vmPosition.getBayNo() - 1, vmPosition.getRowNo(), vmPosition.getTierNo()).getVLocation());
                                    } else {
                                        vmSlotD = cwpData.getStructureData().getVMSlotByVLocation(vmPosition.getVLocation());
                                    }
                                    WorkMove workMoveD = cwpData.getMoveData().getWorkMoveByVMSlot(vmSlotD, workMove.getDlType());
                                    if (workMoveD != null && workMoveD.getMoveOrder() == null && availableWt < 250) { // 说明卸船出翻舱还没有作业
                                        reStowCntTimeL = availableWt;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            //设置属性
            //判断垫脚箱可以连续作业完，判断这个舱内垫脚箱作业的最早时间和最晚时间
            cwpBay.setDpSteppingCntFlag(steppingCntAvailableWt > 0 && steppingCntTotalWt == steppingCntAvailableWt);
            cwpBay.setDpSteppingAvailableWt(steppingCntAvailableWt);
            cwpBay.setDpSteppingTotalWt(steppingCntTotalWt);
            //判断卸完大倍位上的箱子不能接着在大倍位上装，需要进行垫脚箱作业的判断
            cwpBay.setDpAvailableDiscWtD(availableDiscWtD);
            cwpBay.setDpAvailableLoadWtD(availableLoadWtD);
            cwpBay.setDpAvailableDiscWtX(availableDiscWtX);
            cwpBay.setDpAvailableLoadWtX(availableLoadWtX);
            cwpBay.setDpAvailableWorkTime(availableWt);
            // 设置倍位量的装卸类型
            if (loadNum > 0 && discNum == 0) {
                cwpBay.setDpLoadOrDisc(CWPDomain.DL_TYPE_LOAD);
            } else {
                if (availableWt > 0) {
                    cwpBay.setDpLoadOrDisc(CWPDomain.DL_TYPE_DISC);
                }
            }
            // 装船出翻舱在该倍位开始作业的时间
            cwpBay.setReStowCntTimeL(reStowCntTimeL);
            cwpBay.setDpSelectedByCraneNo(null);
        }
        Map<Integer, List<CWPBay>> cwpHatchBayMap = PublicMethod.getCwpHatchBayMap(cwpBayList, cwpData);
        cwpData.setCwpHatchBayMap(cwpHatchBayMap);
    }


    public void firstAnalyzeCwpCrane(CwpData cwpData) {
        long hatchScanTime = PublicMethod.getAvailableCwpBayNum(cwpData.getAllCWPBays()) * cwpData.getWorkingData().getCwpConfig().getHatchScanTime();
        Long totalWorkTime = PublicMethod.getCurTotalWorkTime(cwpData.getAllCWPBays()) + hatchScanTime;
        Long vesselTime = cwpData.getVesselTime();
        int minCraneNum = 0;
        if (vesselTime > 0) {
            minCraneNum = (int) Math.ceil(totalWorkTime.doubleValue() / (vesselTime.doubleValue()));
        } else { //当前时间超过船期时间
            cwpData.getWorkingData().getLogger().logError("当前时间超过船期结束时间，算法无法运行！请检查船期时间！");
        }
        int maxCraneNum = PublicMethod.getMaxCraneNum(cwpData.getAllCWPBays(), cwpData);
        cwpData.getWorkingData().getLogger().logInfo("Minimum number of crane is: " + minCraneNum + ", maximum number of crane is: " + maxCraneNum);

        if (CraneMethod.hasCraneAddOrDelete(cwpData.getAllCWPCranes())) {
            // 1、如果有桥机上/下路，应该在第一次划分范围时提前预留作业倍位
            try {
                analyzeCraneMoveRangeWithAddOrDel(cwpData.getAllCWPCranes(), cwpData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 上手开路可以使用的桥机，考虑加减桥机信息
            List<CWPCrane> availableCraneList = CraneMethod.getFirstAvailableCraneList(cwpData);
            List<CWPCrane> addOrDelCraneList = PublicMethod.copyCwpCraneList(availableCraneList);
            LogPrintMethod.printSelectedCrane(addOrDelCraneList, cwpData.getWorkingData().getLogger());
            removeRedundantDividedBay(addOrDelCraneList, cwpData);
            cwpData.getDpFirstCwpCraneMap().put("addOrDel", addOrDelCraneList);
        } else { // 没有上下路计划
            List<CWPCrane> availableCraneList = cwpData.getAllCWPCranes();
            // 取最多只能容纳的桥机，一般不会发生给的桥机比最大容纳桥机多的情况
            int craneNum = availableCraneList.size() > maxCraneNum ? maxCraneNum : availableCraneList.size();
            List<CWPCrane> dpCraneList = new ArrayList<>();
            for (int i = 0; i < craneNum; i++) {
                dpCraneList.add(availableCraneList.get(i));
            }

            // 如果每个倍位的量，保持安全距离，那么桥机可以作业所有倍位
            boolean specialHatch = true;
            Map<Integer, List<CWPBay>> cwpHatchBayMap = cwpData.getCwpHatchBayMap();
            List<Integer> bayNoList = new ArrayList<>(cwpHatchBayMap.keySet());
            for (int i = 0; i < bayNoList.size() - 1; i++) {
                if (PublicMethod.safeSpanBay(bayNoList.get(i), bayNoList.get(i + 1), cwpData)) {
                    specialHatch = false;
                    break;
                }
            }
            if (specialHatch) {
                List<CWPCrane> specialHatchCraneList = PublicMethod.copyCwpCraneList(dpCraneList);
                try {
                    cwpData.getWorkingData().getLogger().logInfo("Analyze crane move range with specialHatch......");
                    analyzeCraneMoveRangeWithSpecialHatch(specialHatchCraneList, cwpData.getAllCWPBays(), cwpData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LogPrintMethod.printSelectedCrane(specialHatchCraneList, cwpData.getWorkingData().getLogger());
                cwpData.getDpFirstCwpCraneMap().put("specialHatch", specialHatchCraneList);
            } else {
                // 1、默认按平均量分割的方法
                List<CWPCrane> averageCraneList = PublicMethod.copyCwpCraneList(dpCraneList);
                try {
                    cwpData.getWorkingData().getLogger().logInfo("Analyze crane move range with average workTime......");
                    analyzeCraneMoveRangeWithAverage(averageCraneList, cwpData.getAllCWPBays(), cwpData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LogPrintMethod.printSelectedCrane(averageCraneList, cwpData.getWorkingData().getLogger());
                removeRedundantDividedBay(averageCraneList, cwpData);
                if (CWPDomain.YES.equals(cwpData.getWorkingData().getCwpConfig().getCraneSameWorkTime())) {
                    cwpData.getDpFirstCwpCraneMap().put("average", averageCraneList);
                }
                // 2、按最大量分割的方法
                List<CWPCrane> maxRoadCraneList = PublicMethod.copyCwpCraneList(dpCraneList);
                boolean analyzeWithMaxRoad = false;
                try {
                    cwpData.getWorkingData().getLogger().logInfo("Analyze crane move range with max road......");
                    analyzeWithMaxRoad = analyzeCraneMoveRangeWithMaxRoad(maxRoadCraneList, cwpData.getAllCWPBays(), cwpData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (analyzeWithMaxRoad) {
                    LogPrintMethod.printSelectedCrane(maxRoadCraneList, cwpData.getWorkingData().getLogger());
                    removeRedundantDividedBay(maxRoadCraneList, cwpData);
                    if (CWPDomain.YES.equals(cwpData.getWorkingData().getCwpConfig().getMainRoadOneCrane())) {
                        cwpData.getDpFirstCwpCraneMap().put("maxRoad", maxRoadCraneList);
                    }
                }
                // 3、按驾驶台和烟囱位置，桥机作业量不平均（中间桥机晚结束，两边桥机早结束，分割舱最少）
                List<CWPCrane> notSameTimeCraneList = PublicMethod.copyCwpCraneList(dpCraneList);
                boolean analyzeWithNotSameTime = false;
                try {
                    analyzeWithNotSameTime = analyzeCraneMoveRangeWithNotSameTime(notSameTimeCraneList, cwpData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (analyzeWithNotSameTime) {
                    LogPrintMethod.printSelectedCrane(notSameTimeCraneList, cwpData.getWorkingData().getLogger());
                    removeRedundantDividedBay(notSameTimeCraneList, cwpData);
                    if (CWPDomain.YES.equals(cwpData.getWorkingData().getCwpConfig().getCraneNotSameWorkTime())) {
                        cwpData.getDpFirstCwpCraneMap().put("notSameTime", notSameTimeCraneList);
                    }
                }
                // 没有策略参数，设置默认的策略
                if (cwpData.getDpFirstCwpCraneMap().size() == 0) {
                    cwpData.getDpFirstCwpCraneMap().put("average", averageCraneList);
                    if (analyzeWithMaxRoad) {
                        cwpData.getDpFirstCwpCraneMap().put("maxRoad", maxRoadCraneList);
                    }
                    if (analyzeWithNotSameTime) {
                        cwpData.getDpFirstCwpCraneMap().put("notSameTime", notSameTimeCraneList);
                    }
                }
            }
        }

        // 分析桥机第一次关键性开路选择
        analyzeFirstCraneSelectBay(cwpData);
    }

    private void analyzeFirstCraneSelectBay(CwpData cwpData) {
        for (Map.Entry<String, List<CWPCrane>> entry : cwpData.getDpFirstCwpCraneMap().entrySet()) {
            List<CWPCrane> cwpCraneList = entry.getValue();
            for (CWPCrane cwpCrane : cwpCraneList) {
                cwpCrane.getDpCurCanSelectBays().addAll(cwpCrane.getDpFirstCanSelectBays());
            }
            for (CWPCrane cwpCrane : cwpCraneList) {
                Set<Integer> bayNos = new HashSet<>();
                List<WorkBlock> workBlockList = cwpData.getWorkingData().getLockCraneWorkBlockMap().get(cwpCrane.getCraneNo());
                if (workBlockList != null && workBlockList.size() > 0) {
                    bayNos.add(Integer.valueOf(workBlockList.get(0).getBayNo()));
                } else {
                    //按倍位量排序
                    List<CWPBay> cwpBayList = new ArrayList<>();
                    for (Integer bayNo : cwpCrane.getDpCurCanSelectBays()) {
                        CWPBay cwpBay = cwpData.getCWPBayByBayNo(bayNo);
                        if (cwpBay.getDpAvailableWorkTime() > 0) {
                            cwpBayList.add(cwpBay);
                        }
                    }
                    PublicMethod.sortCwpBayByWorkTimeDesc(cwpBayList);
                    for (CWPBay cwpBay : cwpBayList) {
                        if (cwpBay.getReStowCntTimeD() > 0) { // 出翻舱优先作业
                            bayNos.add(cwpBay.getBayNo());
                        }
                    }
                    if (bayNos.size() == 0) { // 没有出翻舱的倍位
                        // 开路装卸参数，按"L,D,LD"选择开路的倍位
                        if (CWPDomain.LOAD_PRIOR_L.equals(cwpData.getWorkingData().getCwpConfig().getLoadPrior())) {
                            for (CWPBay cwpBay : cwpBayList) {
                                if (CWPDomain.DL_TYPE_LOAD.equals(cwpBay.getDpLoadOrDisc())) {
                                    bayNos.add(cwpBay.getBayNo());
                                }
                            }
                        } else if (CWPDomain.LOAD_PRIOR_D.equals(cwpData.getWorkingData().getCwpConfig().getLoadPrior())) {
                            for (CWPBay cwpBay : cwpBayList) {
                                if (CWPDomain.DL_TYPE_DISC.equals(cwpBay.getDpLoadOrDisc())) {
                                    bayNos.add(cwpBay.getBayNo());
                                }
                            }
                        } else {
                            // 量最大的倍位
                            if (cwpBayList.size() > 0) {
                                bayNos.add(cwpBayList.get(0).getBayNo());
                            }
                            // 桥机分界的倍位
                            for (CWPBay cwpBay : cwpBayList) {
                                if (cwpBay.getBayNo() % 2 == 0 && cwpBay.getDpAvailableWorkTime() > 0) {
                                    if (craneSplitBay(cwpCrane, cwpBay, cwpData)) {
                                        bayNos.add(cwpBay.getBayNo());
                                    }
                                }
                            }
                        }
                    }
                }
                cwpCrane.getDpSelectBays().addAll(bayNos);
                // 根据劈路参数设置，缩减分支：针对旁边桥机，如果没有分割倍位、且与相邻桥机在安全距离之外，则上手考虑劈路倍位
            }
        }
    }

    private void analyzeCraneMoveRangeWithAddOrDel(List<CWPCrane> allCwpCranes, CwpData cwpData) {
        cwpData.getWorkingData().getLogger().logInfo("Analyze crane move range with add or delete crane......");
        // 计算上下路桥机的的作业量
        computeAddOrDelCraneWorkTime(allCwpCranes, cwpData);
        // 左边加减桥机、中间平均分、右边加减桥机划分作业范围
        divideByAddOrDelCrane(allCwpCranes, cwpData.getAllCWPBays(), cwpData);

        // 当桥机预留的量不满足桥机安全距离时，保持桥机安全距离，重新划分有问题桥机的作业范围
        divideByAddOrDelCrane1(allCwpCranes, cwpData.getAllCWPBays(), cwpData);
    }

    private void computeAddOrDelCraneWorkTime(List<CWPCrane> allCwpCranes, CwpData cwpData) {
        long hatchScanTime = cwpData.getWorkingData().getCwpConfig().getHatchScanTime();
        long allWorkTime = PublicMethod.getCurTotalWorkTimeWithHatchScanTime(cwpData.getAllCWPBays(), hatchScanTime);
        List<CWPCrane> oneAddCwpCraneList = new ArrayList<>(); // 只上路一次的桥机列表
        List<CWPCrane> noneCwpCraneList = new ArrayList<>(); // 全程一直作业桥机列表
        for (CWPCrane cwpCrane : allCwpCranes) {
            long workTime = 0;
            List<CWPCraneWork> cwpCraneWorkList = cwpCrane.getCwpCraneWorkList();
            if (cwpCraneWorkList.size() > 0) {
                if (cwpCraneWorkList.size() == 1) {
                    CWPCraneWork cwpCraneWork1 = cwpCraneWorkList.get(0);
                    if (CWPDomain.DELETE_CRANE.equals(cwpCraneWork1.getAddOrDeleteFlag())) { // 只减一次
                        workTime = cwpCraneWork1.getAddOrDeleteTime().getTime() / 1000 - cwpData.getDpCurrentTime();
                        cwpCrane.setAddOrDelType(CWPDomain.ONE_DEL);
                    } else { // 只加一次，一直作业到结束
                        cwpCrane.setAddOrDelType(CWPDomain.ONE_ADD);
                        oneAddCwpCraneList.add(cwpCrane);
                    }
                } else if (cwpCraneWorkList.size() == 2) {
                    CWPCraneWork cwpCraneWork1 = cwpCraneWorkList.get(0);
                    CWPCraneWork cwpCraneWork2 = cwpCraneWorkList.get(1);
                    // 中途加桥机，提前下路
                    if (CWPDomain.ADD_CRANE.equals(cwpCraneWork1.getAddOrDeleteFlag()) && CWPDomain.DELETE_CRANE.equals(cwpCraneWork2.getAddOrDeleteFlag())) {
                        workTime = cwpCraneWork2.getAddOrDeleteTime().getTime() / 1000 - cwpCraneWork1.getAddOrDeleteTime().getTime() / 1000;
                        cwpCrane.setAddOrDelType(CWPDomain.ONE_DEL);
                    } else {
                        cwpData.getWorkingData().getLogger().logError("算法暂时不支持同一部桥机先减后加的情况！");
                    }
                }
            } else { // 没有上下路计划的桥机
                noneCwpCraneList.add(cwpCrane);
            }
            cwpCrane.setCraneWorkTime(workTime);
            allWorkTime = allWorkTime - workTime;
        }
        // 只加一次的情况，按加桥机的时间从大到小排序
        Collections.sort(oneAddCwpCraneList, new Comparator<CWPCrane>() {
            @Override
            public int compare(CWPCrane o1, CWPCrane o2) {
                return o2.getCwpCraneWorkList().get(0).getAddOrDeleteTime().compareTo(o1.getCwpCraneWorkList().get(0).getAddOrDeleteTime());
            }
        });
        for (int i = 0; i < oneAddCwpCraneList.size(); i++) {
            CWPCrane cwpCrane = oneAddCwpCraneList.get(i);
            //全装20、全卸25，装卸都有22-24，小大：1：1就取24，小大：1：2就取22
            CWPCraneWork cwpCraneWork = cwpCrane.getCwpCraneWorkList().get(0);
            long wt = noneCwpCraneList.size() * (cwpCraneWork.getAddOrDeleteTime().getTime() / 1000 - cwpData.getDpCurrentTime());
            for (int k = i + 1; k < oneAddCwpCraneList.size(); k++) {
                CWPCrane cwpCrane1 = oneAddCwpCraneList.get(k);
                wt += cwpCraneWork.getAddOrDeleteTime().getTime() / 1000 - cwpCrane1.getCwpCraneWorkList().get(0).getAddOrDeleteTime().getTime() / 1000;
            }
            long workTime = (allWorkTime - wt) / (noneCwpCraneList.size() + oneAddCwpCraneList.size() - i);
            workTime = workTime > 0 ? workTime : 0;
            cwpCrane.setCraneWorkTime(workTime);
            allWorkTime = allWorkTime - workTime;
        }
        // 没有上下路计划的桥机
        for (CWPCrane cwpCrane : noneCwpCraneList) {
            cwpCrane.setCraneWorkTime(allWorkTime / noneCwpCraneList.size());
        }
    }

    private void divideByAddOrDelCrane(List<CWPCrane> allCwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        cwpBays = PublicMethod.getCwpBayListToCwpBayListD(cwpBays, cwpData);

        // 每部桥机选择自己作业的作业量，和人工设置的作业倍位
        int c = 0, cSize = 0;
        int craneNum = allCwpCranes.size();
        int bayNum = cwpBays.size();
        long tmpWorkTime = 0;
        boolean addFlag = false;
        for (int j = 0; j < bayNum; j++) {
            cSize += 1;
            Integer bayNoD = cwpBays.get(j).getBayNo();
            List<CWPBay> cwpBayList = cwpData.getCwpBayListByBayNoD(bayNoD);
            long bayWorkTime = PublicMethod.getCurTotalWorkTimeWithHatchScanTime(cwpBayList, cwpData.getWorkingData().getCwpConfig().getHatchScanTime());
            tmpWorkTime += bayWorkTime;
            c = c == craneNum ? craneNum - 1 : c;
            CWPCrane cwpCrane = allCwpCranes.get(c);
            cwpCrane.setDpWorkBayNoTo(bayNoD);
            for (CWPBay cwpBay : cwpBayList) {
                cwpCrane.getDpFirstCanSelectBays().add(cwpBay.getBayNo());
                cwpCrane.setDpCurrentWorkPosition(cwpBay.getWorkPosition()); //初始化加减桥机的当前作业位置，对加减的桥机第一次开路有关键性作用
            }
            cwpCrane.addDpCurMeanWt(bayWorkTime);
            long mean = cwpCrane.getCraneWorkTime(); // 桥机作业的量
            long meanL = mean - 10 * cwpData.getWorkingData().getCwpConfig().getOneCntTime();
            long meanR = mean + 10 * cwpData.getWorkingData().getCwpConfig().getOneCntTime();
            if (!addFlag && PublicMethod.machineBetweenBay(cwpCrane.getDpFirstCanSelectBays(), cwpData)) { // 倍位中间包含驾驶台/烟囱，则认为桥机会跨
                tmpWorkTime = tmpWorkTime + 2 * cwpData.getWorkingData().getCwpConfig().getCrossBarTime();
                addFlag = true;
            }
            if (tmpWorkTime >= meanL && tmpWorkTime <= meanR) {
                cwpCrane.setDpWorkBayNoFrom(cwpBays.get(j + 1 - cSize).getBayNo());
                cwpCrane.setDpWorkBayNoTo(bayNoD);
                tmpWorkTime = 0L;
                cSize = 0;
                c++;
                addFlag = false;
            } else if (tmpWorkTime > meanR) {
                cwpCrane.setDpWorkBayNoFrom(cwpBays.get(j + 1 - cSize).getBayNo());
                cwpCrane.setDpWorkBayNoTo(bayNoD);
                long wt1 = bayWorkTime - (tmpWorkTime - mean);
                if (c < craneNum - 1) {
                    cwpCrane.setDpWorkTimeTo(wt1);
                    cwpCrane.setDpCurMeanWt(cwpCrane.getDpCurMeanWt() - bayWorkTime + wt1);
                }
                tmpWorkTime = bayWorkTime - wt1;
                c++;
                addFlag = false;
                if (c < craneNum) {
                    CWPCrane cwpCraneNext = allCwpCranes.get(c);
                    cwpCraneNext.setDpWorkTimeFrom(tmpWorkTime);
                    for (CWPBay cwpBay1 : cwpBayList) {
                        cwpCraneNext.getDpFirstCanSelectBays().add(cwpBay1.getBayNo());
                    }
                    cwpCraneNext.addDpCurMeanWt(tmpWorkTime);
                    if (j == bayNum - 1) {
                        if (cwpCraneNext.getDpWorkBayNoFrom() == null) {
                            cwpCraneNext.setDpWorkBayNoFrom(cwpBays.get(j).getBayNo());
                        }
                        if (cwpCraneNext.getDpWorkBayNoTo() == null) {
                            CWPBay nextBay = PublicMethod.getNextBay(cwpBays.get(j), cwpData);
                            cwpCraneNext.setDpWorkBayNoTo(nextBay.getBayNo());
                        }
                    }
                }
                cSize = 1;
            } else {
                if (c == craneNum - 1 && j == bayNum - 1) { //???
                    if (cwpCrane.getDpWorkBayNoFrom() == null) {
                        cwpCrane.setDpWorkBayNoFrom(cwpBays.get(j + 1 - cSize).getBayNo());
                    }
                    if (cwpCrane.getDpWorkBayNoTo() == null) {
                        CWPBay nextBay = PublicMethod.getNextBay(cwpBays.get(j + 1 - cSize), cwpData);
                        cwpCrane.setDpWorkBayNoTo(nextBay.getBayNo());
                    }
                }
            }
        }
        for (int i = allCwpCranes.size() - 1; i >= 0; i--) {
            CWPCrane cwpCrane = allCwpCranes.get(i);
            if (cwpCrane.getCwpCraneWorkList().size() > 0) {
                cwpCrane.setDpCurrentWorkPosition(cwpData.getCWPBayByBayNo(cwpCrane.getDpFirstCanSelectBays().getFirst()).getWorkPosition());
            } else {
                break;
            }
        }
    }

    private void divideByAddOrDelCrane1(List<CWPCrane> allCwpCranes, List<CWPBay> allCWPBays, CwpData cwpData) {
        for (int i = 0; i < allCwpCranes.size(); i++) {
            CWPCrane cwpCrane = allCwpCranes.get(i);
            if (cwpCrane.getAddOrDelType() == null) {
                break;
            }
            // 分情况给上/下路的桥机设置合适作业量的倍位
            if (CWPDomain.ONE_ADD.equals(cwpCrane.getAddOrDelType())) { // 只加一次，一直作业到结束
                if (cwpCrane.getFirstWorkBayNo() != null) {
                    CWPBay cwpBay = cwpData.getCWPBayByBayNo(cwpCrane.getFirstWorkBayNo());
                    if (cwpBay.getDpCurrentTotalWorkTime().compareTo(cwpCrane.getCraneWorkTime()) >= 0) { // 桥机只作业这个倍位的部分量

                    } else { // 桥机继续往两边方向选择倍位作业

                    }
                } else { // 没有设置开工倍位

                }
            } else if (CWPDomain.ONE_DEL.equals(cwpCrane.getAddOrDelType())) { // 只减一次，包括中途加入

            }
        }
    }

    private void analyzeCraneMoveRangeWithSpecialHatch(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        long allWorkTime = PublicMethod.getCurTotalWorkTime(cwpBays);
        for (CWPCrane cwpCrane : cwpCranes) {
            cwpCrane.getDpFirstCanSelectBays().clear();
            for (CWPBay cwpBay : cwpBays) {
                cwpCrane.getDpFirstCanSelectBays().add(cwpBay.getBayNo());
            }
            cwpCrane.setDpCurMeanWt(allWorkTime);
            cwpCrane.setDpWorkBayNoFrom(cwpBays.get(0).getBayNo());
            cwpCrane.setDpWorkBayNoTo(cwpBays.get(cwpBays.size() - 1).getBayNo());
        }
    }

    private void analyzeCraneMoveRangeWithAverage(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        cwpData.getWorkingData().getLogger().logInfo("Analyze crane move range with average workTime");
        if (cwpCranes.size() == 1) {
            divideByOneCraneNum(cwpCranes, cwpBays, cwpData);
        } else {
            int maxCraneNum = PublicMethod.getMaxCraneNum(cwpBays, cwpData);
            if (maxCraneNum == cwpCranes.size()) {
                divideByMaxCraneNum(cwpCranes, cwpBays, cwpData);
            } else {
                divideByAverageWorkTime(cwpCranes, cwpBays, cwpData);
            }
        }
    }

    private boolean analyzeCraneMoveRangeWithMaxRoad(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        boolean divided = false;
        int craneNum = cwpCranes.size();
        if (craneNum >= 3) {
            long allWorkTime = PublicMethod.getCurTotalWorkTime(cwpBays);
//            long hatchScanTime = cwpData.getWorkingData().getCwpConfig().getHatchScanTime();
//            long allWorkTime = PublicMethod.getCurTotalWorkTimeWithHatchScanTime(cwpBays, hatchScanTime);
            long meanWorkTime = allWorkTime / craneNum;
            cwpData.getWorkingData().getLogger().logInfo("Analyze crane move range with max road");
            List<CWPBay> maxCwpBayList = PublicMethod.getMaxWorkTimeCWPBayList(cwpBays, cwpData);
            long maxWorkTime = PublicMethod.getCurTotalWorkTime(maxCwpBayList);
            LogPrintMethod.printMaxCwpBay(maxCwpBayList, cwpData.getWorkingData().getLogger());
            long amount = (cwpData.getVesselTime() - meanWorkTime) / craneNum;
            amount = amount > 1800 ? 1800 : amount;
            cwpData.getWorkingData().getLogger().logInfo("Max workTime: " + maxWorkTime + ", mean workTime: " + meanWorkTime + ", amount: " + amount);
            if (maxWorkTime > meanWorkTime || maxWorkTime > meanWorkTime - amount) {
                List<CWPBay> leftCwpBayList = PublicMethod.getSideCwpBayList(CWPDomain.L, maxCwpBayList, cwpBays);
                List<CWPBay> rightCwpBayList = PublicMethod.getSideCwpBayList(CWPDomain.R, maxCwpBayList, cwpBays);
                Long leftWorkTime = PublicMethod.getCurTotalWorkTime(leftCwpBayList);
                Long rightWorkTime = PublicMethod.getCurTotalWorkTime(rightCwpBayList);
                double left = CalculateUtil.div(leftWorkTime.doubleValue(), cwpData.getVesselTime().doubleValue(), 2);
                double right = CalculateUtil.div(rightWorkTime.doubleValue(), cwpData.getVesselTime().doubleValue(), 2);
                int leftCraneNum = (int) Math.ceil(left);
                int rightCraneNum = (int) Math.ceil(right);
                int redundantNum = craneNum - leftCraneNum - rightCraneNum - 1;
                if (redundantNum >= 0) { //桥机数目有多余，说明两边的桥机够用 3
                    int up = (int) Math.ceil(redundantNum / 2.0); //2
                    int down = redundantNum / 2; //1
                    leftCraneNum = left - Math.floor(left) > right - Math.floor(right) ? leftCraneNum + up : leftCraneNum + down;
                    rightCraneNum = right - Math.floor(right) > left - Math.floor(left) ? rightCraneNum + up : rightCraneNum + down;
                    if (craneNum == leftCraneNum + rightCraneNum + 1) {
                        List<CWPCrane> leftCwpCraneList = new ArrayList<>();
                        List<CWPCrane> maxRoadCwpCraneList = new ArrayList<>();
                        List<CWPCrane> rightCwpCraneList = new ArrayList<>();
                        int i = 0;
                        for (; i < leftCraneNum; i++) {
                            leftCwpCraneList.add(cwpCranes.get(i));
                        }
                        maxRoadCwpCraneList.add(cwpCranes.get(i++));
                        for (; i < craneNum; i++) {
                            rightCwpCraneList.add(cwpCranes.get(i));
                        }
                        boolean d1 = analyzeCraneMoveRangeWithMaxRoad(leftCwpCraneList, leftCwpBayList, cwpData);
                        boolean d2 = analyzeCraneMoveRangeWithMaxRoad(maxRoadCwpCraneList, maxCwpBayList, cwpData);
                        boolean d3 = analyzeCraneMoveRangeWithMaxRoad(rightCwpCraneList, rightCwpBayList, cwpData);
                        divided = d1 && d2 && d3;
                    } else {
                        cwpData.getWorkingData().getLogger().logInfo("Failed! (" + craneNum + ":" + leftCraneNum + "+ 1 +" + rightCraneNum + ").");
                    }
                } else {
                    cwpData.getWorkingData().getLogger().logInfo("Failed! the number of cranes is not enough");
                }
            } else {
                cwpData.getWorkingData().getLogger().logInfo("Failed! max workTime < mean workTime");
            }
        } else {
            if (craneNum == 1) {
                divideByOneCraneNum(cwpCranes, cwpBays, cwpData);
                divided = true;
            } else { //craneNum == 2
                int maxCraneNum = PublicMethod.getMaxCraneNum(cwpBays, cwpData);
                if (maxCraneNum == craneNum) {
                    divideByMaxCraneNum(cwpCranes, cwpBays, cwpData);
                    divided = true;
                } else {
                    divideByAverageWorkTime(cwpCranes, cwpBays, cwpData);
                    divided = true;
                }
            }
        }
        return divided;
    }

    private boolean analyzeCraneMoveRangeWithNotSameTime(List<CWPCrane> cwpCranes, CwpData cwpData) {
        boolean divided = false;
        int craneNum = cwpCranes.size();
        cwpData.getWorkingData().getLogger().logInfo("Analyze crane move range with not same workTime......");
        Map<Integer, List<CWPBay>> bayRangeMap = PublicMethod.getBayRangeMapByMachine(cwpData); //按驾驶台/烟囱分区块
        Long vesselTime = cwpData.getVesselTime();
        if (bayRangeMap.size() == 2) {
            int key0 = 0, key1 = 1;
            if (bayRangeMap.get(0) == null) {
                key0 = 1;
                key1 = 2;
            }
            long wt0 = PublicMethod.getCurTotalWorkTime(bayRangeMap.get(key0));
            long wt1 = PublicMethod.getCurTotalWorkTime(bayRangeMap.get(key1));
            double d0 = CalculateUtil.div(wt0, vesselTime, 2);
            double d1 = CalculateUtil.div(wt1, vesselTime, 2);
            int craneNum0 = (int) Math.ceil(d0);
            int craneNum1 = (int) Math.ceil(d1);
            int redundantNum = craneNum - craneNum0 - craneNum1;
            if (redundantNum >= 0) {
                if (redundantNum == 1) {
                    craneNum0 += wt0 > wt1 ? redundantNum : 0;
                } else if (redundantNum == 2) {
                    craneNum0 += 1;
                } else if (redundantNum == 3) {
                    craneNum0 += wt0 > wt1 ? 2 : 1;
                } else {
                    craneNum0 += wt0 > wt1 ? redundantNum : 0;
                }
                List<CWPCrane> craneList0 = new ArrayList<>();
                List<CWPCrane> craneList1 = new ArrayList<>();
                int i = 0;
                for (; i < craneNum0; i++) {
                    craneList0.add(cwpCranes.get(i));
                }
                for (; i < craneNum; i++) {
                    craneList1.add(cwpCranes.get(i));
                }
                if (!analyzeCraneMoveRangeWithMaxRoad(craneList0, bayRangeMap.get(key0), cwpData)) {
                    analyzeCraneMoveRangeWithAverage(craneList0, bayRangeMap.get(key0), cwpData);
                }
                if (!analyzeCraneMoveRangeWithMaxRoad(craneList1, bayRangeMap.get(key1), cwpData)) {
                    analyzeCraneMoveRangeWithAverage(craneList1, bayRangeMap.get(key1), cwpData);
                }
                divided = true;
            } else {
                cwpData.getWorkingData().getLogger().logInfo("Failed! the number of cranes is not enough");
            }
        } else if (bayRangeMap.size() == 3) {
            long wt0 = PublicMethod.getCurTotalWorkTime(bayRangeMap.get(0));
            long wt1 = PublicMethod.getCurTotalWorkTime(bayRangeMap.get(1));
            long wt2 = PublicMethod.getCurTotalWorkTime(bayRangeMap.get(2));
            int craneNum0 = (int) Math.ceil(CalculateUtil.div(wt0, vesselTime, 2));
            int craneNum1 = (int) Math.ceil(CalculateUtil.div(wt1, vesselTime, 2));
            int craneNum2 = (int) Math.ceil(CalculateUtil.div(wt2, vesselTime, 2));
            int redundantNum = craneNum - craneNum0 - craneNum1 - craneNum2;
            if (redundantNum >= 0) {
                craneNum1 += redundantNum; //todo:多余的桥机给谁
                List<CWPCrane> craneList0 = new ArrayList<>();
                List<CWPCrane> craneList1 = new ArrayList<>();
                List<CWPCrane> craneList2 = new ArrayList<>();
                int i = 0;
                for (; i < craneNum0; i++) {
                    craneList0.add(cwpCranes.get(i));
                }
                for (; i < craneNum0 + craneNum1; i++) {
                    craneList1.add(cwpCranes.get(i));
                }
                for (; i < craneNum; i++) {
                    craneList2.add(cwpCranes.get(i));
                }
                if (!analyzeCraneMoveRangeWithMaxRoad(craneList0, bayRangeMap.get(0), cwpData)) {
                    analyzeCraneMoveRangeWithAverage(craneList0, bayRangeMap.get(0), cwpData);
                }
                if (!analyzeCraneMoveRangeWithMaxRoad(craneList1, bayRangeMap.get(1), cwpData)) {
                    analyzeCraneMoveRangeWithAverage(craneList1, bayRangeMap.get(1), cwpData);
                }
                if (!analyzeCraneMoveRangeWithMaxRoad(craneList2, bayRangeMap.get(2), cwpData)) {
                    analyzeCraneMoveRangeWithAverage(craneList2, bayRangeMap.get(2), cwpData);
                }
                divided = true;
            } else {
                cwpData.getWorkingData().getLogger().logInfo("Failed! the number of cranes is not enough");
            }
        }
        return divided;
    }


    private void divideByAverageWorkTime(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        cwpBays = PublicMethod.getCwpBayListToCwpBayListD(cwpBays, cwpData);
        int craneNum = cwpCranes.size();
        int bayNum = cwpBays.size();
        long allWorkTime = 0;
        long hatchScanTime = cwpData.getWorkingData().getCwpConfig().getHatchScanTime();
        for (CWPBay cwpBay : cwpBays) {
            allWorkTime += PublicMethod.getCurTotalWorkTime(cwpData.getCwpBayListByBayNoD(cwpBay.getBayNo()));
//            allWorkTime += PublicMethod.getCurTotalWorkTimeWithHatchScanTime(cwpData.getCwpBayListByBayNoD(cwpBay.getBayNo()), hatchScanTime);
        }
        long meanWorkTime = allWorkTime / craneNum;
        long amount = 15 * cwpData.getWorkingData().getCwpConfig().getOneCntTime();
        amount = meanWorkTime <= amount ? 0 : amount;
        long meanL = meanWorkTime - amount;
        long meanR = meanWorkTime + amount;
        cwpData.getWorkingData().getLogger().logInfo("Divide by average work time, all workTime：" + allWorkTime + ", mean workTime：" + meanWorkTime + ", amount：" + amount);
        cwpData.getWorkingData().getLogger().logInfo("CraneNo: " + PublicMethod.getCraneNoStr(cwpCranes) + ", BayNo: " + PublicMethod.getBayNoStr(cwpBays));
        int c = 0;
        int cSize = 0;
        long tmpWorkTime = 0;
        long meanLittleOrMore = 0;
        for (CWPCrane cwpCrane : cwpCranes) {
            cwpCrane.getDpFirstCanSelectBays().clear();
        }
        for (int j = 0; j < bayNum; j++) { //只有大倍
            CWPBay cwpBayD = cwpBays.get(j);
            long bayWorkTime = PublicMethod.getCurTotalWorkTime(cwpData.getCwpBayListByBayNoD(cwpBayD.getBayNo()));
//            long bayWorkTime = PublicMethod.getCurTotalWorkTimeWithHatchScanTime(cwpData.getCwpBayListByBayNoD(cwpBayD.getBayNo()), hatchScanTime);
            cSize += 1;
            tmpWorkTime += bayWorkTime;
            c = c == craneNum ? craneNum - 1 : c;
            CWPCrane cwpCrane = cwpCranes.get(c);
            cwpCrane.setDpWorkBayNoTo(cwpBayD.getBayNo());
            for (CWPBay cwpBay : cwpData.getCwpBayListByBayNoD(cwpBayD.getBayNo())) {
                cwpCrane.getDpFirstCanSelectBays().add(cwpBay.getBayNo());
            }
            cwpCrane.addDpCurMeanWt(bayWorkTime);
            if (tmpWorkTime >= meanL && tmpWorkTime <= meanR) {
                cwpCrane.setDpWorkBayNoFrom(cwpBays.get(j + 1 - cSize).getBayNo());
                cwpCrane.setDpWorkBayNoTo(cwpBayD.getBayNo());
                meanLittleOrMore = (tmpWorkTime - meanWorkTime) / craneNum - c - 1;
                tmpWorkTime = 0L;
                cSize = 0;
                c++;
            } else if (tmpWorkTime > meanR) {
                cwpCrane.setDpWorkBayNoFrom(cwpBays.get(j + 1 - cSize).getBayNo());
                cwpCrane.setDpWorkBayNoTo(cwpBayD.getBayNo());
                meanWorkTime += 0L - meanLittleOrMore;
                long wt = bayWorkTime - tmpWorkTime + meanWorkTime;
                Long dwt = getDividedWorkTime(wt, cwpBayD, cwpData); //甲板作为分割量
                wt = dwt != null ? dwt : wt;
                if (c < craneNum - 1) {
                    cwpCrane.setDpWorkTimeTo(wt);
                    cwpCrane.setDpCurMeanWt(cwpCrane.getDpCurMeanWt() - bayWorkTime + wt);
                }
                tmpWorkTime = bayWorkTime - wt;
                c++;
                if (c < craneNum) {
                    CWPCrane cwpCraneNext = cwpCranes.get(c);
                    cwpCraneNext.setDpWorkTimeFrom(tmpWorkTime);
                    for (CWPBay cwpBay1 : cwpData.getCwpBayListByBayNoD(cwpBayD.getBayNo())) {
                        cwpCraneNext.getDpFirstCanSelectBays().add(cwpBay1.getBayNo());
                    }
                    cwpCraneNext.addDpCurMeanWt(tmpWorkTime);
                }
                cSize = 1;
            } else {
                if (c == craneNum - 1 && j == bayNum - 1) { //???
                    if (cwpCrane.getDpWorkBayNoFrom() == null) {
                        cwpCrane.setDpWorkBayNoFrom(cwpBays.get(j + 1 - cSize).getBayNo());
                    }
                    if (cwpCrane.getDpWorkBayNoTo() == null) {
                        CWPBay nextBay = PublicMethod.getNextBay(cwpBays.get(j + 1 - cSize), cwpData);
                        cwpCrane.setDpWorkBayNoTo(nextBay.getBayNo());
                    }
                }
            }
        }
    }

    private void divideByMaxCraneNum(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        cwpBays = PublicMethod.getCwpBayListByWtNotZero(cwpBays);
        int craneNum = cwpCranes.size();
        int bayNum = cwpBays.size();
        if (craneNum <= 0 || bayNum <= 0) {
            return;
        }
        long allWorkTime = PublicMethod.getCurTotalWorkTime(cwpBays);
        cwpData.getWorkingData().getLogger().logInfo("Divide by maximum crane number, all workTime：" + allWorkTime);
        cwpData.getWorkingData().getLogger().logInfo("CraneNo: " + PublicMethod.getCraneNoStr(cwpCranes) + ", BayNo: " + PublicMethod.getBayNoStr(cwpBays));
        int c = 0;
        for (CWPCrane cwpCrane : cwpCranes) {
            cwpCrane.getDpFirstCanSelectBays().clear();
        }
        for (int j = 0; j < bayNum; ) {
            c = c == craneNum ? craneNum - 1 : c;
            CWPBay cwpBayJ = cwpBays.get(j);
            CWPCrane cwpCrane = cwpCranes.get(c);
            if (cwpBayJ.getDpCurrentTotalWorkTime() > 0) {
                cwpCrane.setDpWorkBayNoFrom(cwpBayJ.getBayNo());
                int k = j;
                for (; k < bayNum; k++) {
                    CWPBay cwpBayK = cwpBays.get(k);
                    double distance = CalculateUtil.sub(cwpBayK.getWorkPosition(), cwpBayJ.getWorkPosition());
                    if (distance >= 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance()) {
                        break;
                    } else {
                        cwpCrane.setDpWorkBayNoTo(cwpBayK.getBayNo());
                        cwpCrane.getDpFirstCanSelectBays().add(cwpBayK.getBayNo());
                        cwpCrane.addDpCurMeanWt(cwpBayK.getDpCurrentTotalWorkTime());
                    }
                }
                j = k;
                c++;
            } else {
                j++;
            }
        }
    }

    private void divideByOneCraneNum(List<CWPCrane> cwpCranes, List<CWPBay> cwpBays, CwpData cwpData) {
        cwpBays = PublicMethod.getCwpBayListByWtNotZero(cwpBays);
        int craneNum = cwpCranes.size();
        if (craneNum <= 0 || cwpBays.size() <= 0) {
            return;
        }
        long allWorkTime = PublicMethod.getCurTotalWorkTime(cwpBays);
        cwpData.getWorkingData().getLogger().logInfo("Divide by one crane number, all workTime：" + allWorkTime);
        cwpData.getWorkingData().getLogger().logInfo("CraneNo: " + PublicMethod.getCraneNoStr(cwpCranes) + ", BayNo: " + PublicMethod.getBayNoStr(cwpBays));
        if (craneNum == 1) {
            CWPCrane cwpCrane = cwpCranes.get(0);
            cwpCrane.getDpFirstCanSelectBays().clear();
            for (CWPBay cwpBay : cwpBays) {
                cwpCrane.getDpFirstCanSelectBays().add(cwpBay.getBayNo());
            }
            cwpCrane.setDpCurMeanWt(allWorkTime);
            cwpCrane.setDpWorkBayNoFrom(cwpBays.get(0).getBayNo());
            cwpCrane.setDpWorkBayNoTo(cwpBays.get(cwpBays.size() - 1).getBayNo());
        }
    }

    private void removeRedundantDividedBay(List<CWPCrane> cwpCranes, CwpData cwpData) {
        boolean change = false;
        for (CWPCrane cwpCrane : cwpCranes) {
            CWPCrane frontCrane = PublicMethod.getFrontCrane(cwpCrane, cwpCranes);
            CWPCrane nextCrane = PublicMethod.getNextCrane(cwpCrane, cwpCranes);
            // 1、桥机只有一个大倍位的作业量，判断旁边桥机的作业范围是否在这个大倍位的安全作业范围内
            // 2、桥机只有一条作业路范围，不应该存在分割倍位
            boolean oneHatch = false, oneRoad = false;
            Set<Long> hatchIdSet = new LinkedHashSet<>();
            for (Integer bayNo : cwpCrane.getDpFirstCanSelectBays()) {
                hatchIdSet.add(cwpData.getCWPBayByBayNo(bayNo).getHatchId());
            }
            Integer bayNoFirst = null, bayNoLast = null;
            List<Long> hatchIdList = new ArrayList<>(hatchIdSet);
            if (hatchIdSet.size() == 1) {
                oneHatch = true;
                bayNoFirst = cwpData.getStructureData().getVMHatchByHatchId(hatchIdList.get(0)).getBayNoD();
                bayNoLast = cwpData.getStructureData().getVMHatchByHatchId(hatchIdList.get(0)).getBayNoD();
            } else if (hatchIdSet.size() == 2) { // 两个舱是在安全距离之内的
                bayNoFirst = cwpData.getStructureData().getVMHatchByHatchId(hatchIdList.get(0)).getBayNoD();
                bayNoLast = cwpData.getStructureData().getVMHatchByHatchId(hatchIdList.get(1)).getBayNoD();
                if (PublicMethod.safeSpanBay(bayNoFirst, bayNoLast, cwpData)) {
                    oneRoad = true;
                }
            }
            if ((oneHatch || oneRoad) && bayNoFirst != null && bayNoLast != null) { // 桥机作业范围在一个舱内，且判断一下安全距离内左右桥机的作业倍位
                if (frontCrane != null) {
                    List<Integer> bayNoList = new ArrayList<>();
                    for (int j = frontCrane.getDpFirstCanSelectBays().size() - 1; j >= 0; j--) {
                        Integer bayNo = frontCrane.getDpFirstCanSelectBays().get(j);
                        if (safeCondition(oneHatch, bayNo, bayNoFirst, bayNoList, cwpData)) break;
                    }
                    for (Integer bayNo : bayNoList) {
                        frontCrane.getDpFirstCanSelectBays().remove(bayNo);
                        frontCrane.setDpCurMeanWt(frontCrane.getDpCurMeanWt() - cwpData.getCWPBayByBayNo(bayNo).getDpCurrentTotalWorkTime());
                        if (frontCrane.getDpFirstCanSelectBays().size() > 0) {
                            frontCrane.setDpWorkBayNoTo(frontCrane.getDpFirstCanSelectBays().getLast());
                        }
                        cwpCrane.getDpFirstCanSelectBays().addFirst(bayNo);
                        cwpCrane.setDpCurMeanWt(cwpCrane.getDpCurMeanWt() + cwpData.getCWPBayByBayNo(bayNo).getDpCurrentTotalWorkTime());
                        cwpCrane.setDpWorkBayNoFrom(cwpCrane.getDpFirstCanSelectBays().getFirst());
                        change = true;
                    }
                }
                if (nextCrane != null) {
                    List<Integer> bayNoList = new ArrayList<>();
                    for (Integer bayNo : nextCrane.getDpFirstCanSelectBays()) {
                        if (safeCondition(oneHatch, bayNo, bayNoLast, bayNoList, cwpData)) break;
                    }
                    for (Integer bayNo : bayNoList) {
                        nextCrane.getDpFirstCanSelectBays().remove(bayNo);
                        nextCrane.setDpCurMeanWt(nextCrane.getDpCurMeanWt() - cwpData.getCWPBayByBayNo(bayNo).getDpCurrentTotalWorkTime());
                        if (nextCrane.getDpFirstCanSelectBays().size() > 0) {
                            nextCrane.setDpWorkBayNoFrom(nextCrane.getDpFirstCanSelectBays().getFirst());
                        }
                        cwpCrane.getDpFirstCanSelectBays().addLast(bayNo);
                        cwpCrane.setDpCurMeanWt(cwpCrane.getDpCurMeanWt() + cwpData.getCWPBayByBayNo(bayNo).getDpCurrentTotalWorkTime());
                        cwpCrane.setDpWorkBayNoTo(cwpCrane.getDpFirstCanSelectBays().getLast());
                        change = true;
                    }
                }
            }
        }
        if (change) {
            LogPrintMethod.printSelectedCrane(cwpCranes, cwpData.getWorkingData().getLogger());
        }
    }

    private boolean safeCondition(boolean oneHatch, Integer bayNo, Integer bayNoSide, List<Integer> bayNoList, CwpData cwpData) {
        boolean safeCondition = oneHatch ? PublicMethod.safeSpanBay(bayNo, bayNoSide, cwpData) : cwpData.getCWPBayByBayNo(bayNo).getHatchId().equals(cwpData.getCWPBayByBayNo(bayNoSide).getHatchId());
        if (safeCondition) {
            bayNoList.add(bayNo);
        } else {
            return true;
        }
        return false;
    }


    public void analyzeCwpCrane(CwpData cwpData) {

        List<CWPCrane> availableCraneList = CraneMethod.getAvailableCraneList(cwpData.getDpCwpCraneList(), cwpData);

        availableCraneList = analyzeAutoDelCrane(availableCraneList, cwpData);

        changeCraneMoveRangeByDividedBay(availableCraneList, cwpData);

        analyzeCraneCurCanSelectBay(availableCraneList, cwpData);

        analyzeCraneWaitNotWork(availableCraneList, cwpData);

        cwpData.setDpCwpCraneList(availableCraneList);
    }

    private List<CWPCrane> analyzeAutoDelCrane(List<CWPCrane> cwpCraneList, CwpData cwpData) {
        Long totalWorkTime = PublicMethod.getCurTotalWorkTimeWithHatchScanTime(cwpData.getAllCWPBays(), cwpData.getWorkingData().getCwpConfig().getHatchScanTime());
        Long vesselTime = cwpData.getVesselTime();
        double minCraneNum = 0;
        if (vesselTime > 0) {
            minCraneNum = CalculateUtil.div(totalWorkTime.doubleValue(), vesselTime.doubleValue(), 2);
        } else {
            cwpData.getWorkingData().getLogger().logInfo("当前时间超过船期结束时间！");
        }
        Map<Integer, List<CWPBay>> everyRoadBayMap = PublicMethod.getCurEveryRoadBayMap(cwpData.getAllCWPBays(), cwpData);
        int maxCraneNum = everyRoadBayMap.size();
        cwpData.getWorkingData().getLogger().logDebug("Minimum number of crane is: " + minCraneNum + ", maximum number of crane is: " + maxCraneNum);

        // 去掉没必要的作业路：量少于4块舱盖板时间、且这个舱上次没被桥机选中作业
        Map<Integer, List<CWPBay>> availableEveryRoadBayMap = getAvailableEveryRoadBayMap(everyRoadBayMap, cwpData);
        int availableMaxCraneNum = availableEveryRoadBayMap.size();
        // 上次有效的桥机数目：上次选中了倍位作业，或者没有选中作业倍位但是被设置为等待状态的桥机
        List<CWPCrane> lastAvailableCraneList = getLastAvailableCraneList(cwpCraneList, cwpData);
        cwpData.getWorkingData().getLogger().logDebug("The available maximum number of crane is: " + availableMaxCraneNum + ", last available number of crane is: " + lastAvailableCraneList.size());

        // 当前桥机数目大于或者等于最大可放桥机数目，分析每条作业路被哪部桥机作业，判断旁边桥机是否可以下路
        List<CWPCrane> availableCraneList = new ArrayList<>();
        if (lastAvailableCraneList.size() > availableMaxCraneNum) { // 上次有效的桥机数目大于当前有效的倍位作业路数
            analyzeCraneMoveRangeWithAvailableCrane(lastAvailableCraneList, everyRoadBayMap, cwpData); // 倍位作业路就近原则选择划分给哪部桥机
            for (CWPCrane cwpCrane : lastAvailableCraneList) {
                if (cwpCrane.getDpFirstCanSelectBays().size() > 0) {
                    availableCraneList.add(cwpCrane);
                } else {
                    cwpData.getWorkingData().getLogger().logDebug("桥机(" + cwpCrane.getCraneNo() + ")自动下路！");
                }
            }
        } else {
            availableCraneList.addAll(cwpCraneList);
        }

        return availableCraneList;
    }

    private void analyzeCraneMoveRangeWithAvailableCrane(List<CWPCrane> availableCraneList, Map<Integer, List<CWPBay>> everyRoadBayMap, CwpData cwpData) {
        for (CWPCrane cwpCrane : availableCraneList) {
            cwpCrane.setDpCurMeanWt(0L);
            cwpCrane.setDpWorkBayNoFrom(null);
            cwpCrane.setDpWorkBayNoTo(null);
            cwpCrane.setDpWorkTimeFrom(0L);
            cwpCrane.setDpWorkTimeTo(0L);
            cwpCrane.getDpFirstCanSelectBays().clear();
        }
        for (Map.Entry<Integer, List<CWPBay>> entry : everyRoadBayMap.entrySet()) {
            CWPCrane cwpCrane = null; // 离倍位作业路距离最近的桥机有哪些
            double minDistance = Double.MAX_VALUE;
            for (CWPCrane cwpCrane1 : availableCraneList) {
                CWPBay poBay = cwpData.getCWPBayByBayNo(cwpCrane1.getDpCurrentWorkBayNo());
                double distance = 0;
                for (CWPBay cwpBay1 : entry.getValue()) {
                    distance = CalculateUtil.add(distance, Math.abs(CalculateUtil.sub(poBay.getWorkPosition(), cwpBay1.getWorkPosition())));
                }
                if (distance < minDistance) {
                    minDistance = distance;
                    cwpCrane = cwpCrane1;
                }
            }
            if (cwpCrane != null) {
                for (CWPBay cwpBay1 : entry.getValue()) {
                    cwpCrane.getDpFirstCanSelectBays().add(cwpBay1.getBayNo());
                }
            }
        }
        // 如果减掉的是中间的桥机，则需要往作业量小的那边替换桥机
        List<CWPCrane> middleCwpCraneList = new ArrayList<>();
        for (CWPCrane cwpCrane1 : availableCraneList) {
            if (cwpCrane1.getDpFirstCanSelectBays().size() == 0) {
                middleCwpCraneList.add(cwpCrane1);
            }
        }
        if (middleCwpCraneList.size() == 1) {
            CWPCrane cwpCraneMiddle = middleCwpCraneList.get(0);
            boolean middle = PublicMethod.getFrontCrane(cwpCraneMiddle, availableCraneList) != null && PublicMethod.getNextCrane(cwpCraneMiddle, availableCraneList) != null;
            if (middle) {
                long wtF = 0;
                long wtN = 0;
                List<CWPCrane> cwpCraneListF = new ArrayList<>();
                List<CWPCrane> cwpCraneListN = new ArrayList<>();
                for (CWPCrane cwpCrane1 : availableCraneList) {
                    if (cwpCrane1.getCraneSeq().compareTo(cwpCraneMiddle.getCraneSeq()) < 0) {
                        cwpCraneListF.add(cwpCrane1);
                        for (Integer bayNo : cwpCrane1.getDpFirstCanSelectBays()) {
                            CWPBay cwpBay = cwpData.getCWPBayByBayNo(bayNo);
                            wtF += cwpBay.getDpCurrentTotalWorkTime();
                        }
                    }
                    if (cwpCrane1.getCraneSeq().compareTo(cwpCraneMiddle.getCraneSeq()) > 0) {
                        cwpCraneListN.add(cwpCrane1);
                        for (Integer bayNo : cwpCrane1.getDpFirstCanSelectBays()) {
                            CWPBay cwpBay = cwpData.getCWPBayByBayNo(bayNo);
                            wtN = wtN + cwpBay.getDpCurrentTotalWorkTime();
                        }
                    }
                }
                // 判断两边较少作业量是多少，少于两个小时不替换
                long wt = wtF < wtN ? wtF : wtN;
                if (wt > 7200) {
                    // 判断哪边作业量少
                    if (wtF < wtN) {
                        cwpCraneMiddle.getDpFirstCanSelectBays().addAll(cwpCraneListF.get(cwpCraneListF.size() - 1).getDpFirstCanSelectBays());
                        if (cwpCraneListF.size() > 1) {
                            for (int i = cwpCraneListF.size() - 1; i > 0; i--) {
                                cwpCraneListF.get(i).getDpFirstCanSelectBays().clear();
                                cwpCraneListF.get(i).getDpFirstCanSelectBays().addAll(cwpCraneListF.get(i - 1).getDpFirstCanSelectBays());
                            }
                        } else {
                            cwpCraneListF.get(0).getDpFirstCanSelectBays().clear();
                        }
                    } else {
                        cwpCraneMiddle.getDpFirstCanSelectBays().addAll(cwpCraneListN.get(0).getDpFirstCanSelectBays());
                        if (cwpCraneListN.size() > 1) {
                            for (int i = 0; i < cwpCraneListN.size() - 1; i++) {
                                cwpCraneListN.get(i).getDpFirstCanSelectBays().clear();
                                cwpCraneListN.get(i).getDpFirstCanSelectBays().addAll(cwpCraneListN.get(i + 1).getDpFirstCanSelectBays());
                            }
                        } else {
                            cwpCraneListN.get(0).getDpFirstCanSelectBays().clear();
                        }
                    }
                }
            }
        }
        for (CWPCrane cwpCrane : availableCraneList) {
            if (cwpCrane.getDpFirstCanSelectBays().size() > 0) {
                cwpCrane.setDpWorkBayNoFrom(cwpCrane.getDpFirstCanSelectBays().getFirst());
                cwpCrane.setDpWorkBayNoTo(cwpCrane.getDpFirstCanSelectBays().getLast());
            }
        }
    }

    private Map<Integer, List<CWPBay>> getAvailableEveryRoadBayMap(Map<Integer, List<CWPBay>> everyRoadBayMap, CwpData cwpData) {
        Map<Integer, List<CWPBay>> availableEveryRoadBayMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<CWPBay>> entry : everyRoadBayMap.entrySet()) {
            long wt = PublicMethod.getCurTotalWorkTime(entry.getValue());
            if (wt < 4 * cwpData.getWorkingData().getCwpConfig().getHatchCoverTimeD()) {
                boolean selected = false;
                for (DPPair dpPair : cwpData.getDpResult().getDpTraceBack()) {
                    CWPBay cwpBay = cwpData.getCWPBayByBayNo((Integer) dpPair.getSecond());
                    if (cwpBay != null) {
                        for (CWPBay cwpBay1 : entry.getValue()) {
                            if (cwpBay1.getHatchId().equals(cwpBay.getHatchId())) {
                                selected = true;
                                break;
                            }
                        }
                    }
                }
                if (selected) {
                    availableEveryRoadBayMap.put(entry.getKey(), entry.getValue());
                }
            } else {
                availableEveryRoadBayMap.put(entry.getKey(), entry.getValue());
            }
        }
        return availableEveryRoadBayMap;
    }

    private List<CWPCrane> getLastAvailableCraneList(List<CWPCrane> cwpCraneList, CwpData cwpData) {
        List<CWPCrane> lastAvailableCraneList = new ArrayList<>();
        for (CWPCrane cwpCrane : cwpCraneList) {
            if (PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult()) != null) {
                lastAvailableCraneList.add(cwpCrane);
            } else {
                if (cwpCrane.getDpWait()) {
                    lastAvailableCraneList.add(cwpCrane);
                }
            }
        }
        return lastAvailableCraneList;
    }


    private void changeCraneMoveRangeByDividedBay(List<CWPCrane> availableCraneList, CwpData cwpData) {
        // 针对分割倍位的量，改变桥机第一次分块的作业范围
        for (CWPCrane cwpCrane : availableCraneList) {
            List<Integer> removeBayNos = new ArrayList<>();
            // 去掉没有量的倍位
            for (Integer bayNo : cwpCrane.getDpFirstCanSelectBays()) {
                CWPBay cwpBay = cwpData.getCWPBayByBayNo(bayNo);
                if (cwpBay.getDpCurrentTotalWorkTime() == 0) {
                    removeBayNos.add(bayNo);
                }
            }
            cwpCrane.getDpFirstCanSelectBays().removeAll(removeBayNos);
            // 分割倍位量完成
            removeBayNos.clear();
            for (Integer bayNo : cwpCrane.getDpFirstCanSelectBays()) {
                CWPBay cwpBay = cwpData.getCWPBayByBayNo(bayNo);
                if (isDividedBay(cwpCrane, cwpBay, availableCraneList)) {
                    if (bayNo.equals(cwpCrane.getDpWorkBayNoFrom()) && cwpCrane.getDpWorkTimeFrom().compareTo(cwpData.getWorkingData().getCwpConfig().getOneCntTime()) <= 0) {
                        CWPBay cwpBaySide = getSideBayInFirstSelectBays(bayNo, cwpCrane, CWPDomain.R, cwpData);
                        if (cwpBaySide != null) {
                            removeBayNos.add(bayNo);
                            cwpCrane.setDpWorkBayNoFrom(cwpBaySide.getBayNo());
                        }
                    }
                    if (bayNo.equals(cwpCrane.getDpWorkBayNoTo()) && cwpCrane.getDpWorkTimeTo().compareTo(cwpData.getWorkingData().getCwpConfig().getOneCntTime()) <= 0) {
                        CWPBay cwpBaySide = getSideBayInFirstSelectBays(bayNo, cwpCrane, CWPDomain.L, cwpData);
                        if (cwpBaySide != null) {
                            removeBayNos.add(bayNo);
                            cwpCrane.setDpWorkBayNoTo(cwpBaySide.getBayNo());
                        }
                    }
                }
            }
            cwpCrane.getDpFirstCanSelectBays().removeAll(removeBayNos);
            // 保证FirstCanSelectBays的倍位是桥机作业范围之内
            removeBayNos.clear();
            for (Integer bayNo : cwpCrane.getDpFirstCanSelectBays()) {
                CWPBay cwpBay = cwpData.getCWPBayByBayNo(bayNo);
                if (cwpBay.getWorkPosition().compareTo(cwpData.getCWPBayByBayNo(cwpCrane.getDpWorkBayNoFrom()).getWorkPosition()) < 0) {
                    removeBayNos.add(bayNo);
                }
                if (cwpBay.getWorkPosition().compareTo(cwpData.getCWPBayByBayNo(cwpCrane.getDpWorkBayNoTo()).getWorkPosition()) > 0) {
                    removeBayNos.add(bayNo);
                }
            }
            cwpCrane.getDpFirstCanSelectBays().removeAll(removeBayNos);
        }
    }

    private CWPBay getSideBayInFirstSelectBays(Integer bayNo, CWPCrane cwpCrane, String side, CwpData cwpData) {
        List<Integer> bayNoList = cwpCrane.getDpFirstCanSelectBays();
        for (int i = 0; i < bayNoList.size(); i++) {
            if (bayNo.equals(bayNoList.get(i))) {
                if (CWPDomain.L.equals(side) && i - 1 >= 0) {
                    CWPBay cwpBaySide = cwpData.getCWPBayByBayNo(bayNoList.get(i - 1));
                    if ((PublicMethod.isLittleBay(cwpBaySide, cwpData) && cwpBaySide.getDpAvailableWorkTime() == 0)) { // 小倍位且不可作业，继续找下一个倍位
                        cwpBaySide = getSideBayInFirstSelectBays(cwpBaySide.getBayNo(), cwpCrane, side, cwpData);
                    }
                    return cwpBaySide;
                }
                if (CWPDomain.R.equals(side) && i + 1 <= bayNoList.size() - 1) {
                    CWPBay cwpBaySide = cwpData.getCWPBayByBayNo(bayNoList.get(i + 1));
                    if ((PublicMethod.isLittleBay(cwpBaySide, cwpData) && cwpBaySide.getDpAvailableWorkTime() == 0)) { // 小倍位且不可作业，继续找下一个倍位
                        cwpBaySide = getSideBayInFirstSelectBays(cwpBaySide.getBayNo(), cwpCrane, side, cwpData);
                    }
                    return cwpBaySide;
                }
            }
        }
        return null;
    }


    private void analyzeCraneCurCanSelectBay(List<CWPCrane> availableCraneList, CwpData cwpData) {
        // 分析桥机作业范围内，桥机选择倍位的合理性进行处理（将分界倍位的小倍位加进来）
        for (CWPCrane cwpCrane : availableCraneList) {
            cwpCrane.getDpCurCanSelectBays().clear();
            cwpCrane.getDpCurCanSelectBays().addAll(cwpCrane.getDpFirstCanSelectBays());
        }
        for (CWPCrane cwpCrane : availableCraneList) {
            if (cwpCrane.getDpFirstCanSelectBays().size() > 0) {
                CWPBay cwpBayFirst = cwpData.getCWPBayByBayNo(cwpCrane.getDpFirstCanSelectBays().getFirst());
                CWPBay cwpBayFrom = cwpData.getCWPBayByBayNo(cwpCrane.getDpWorkBayNoFrom());
                if (cwpBayFirst.getBayNo() % 2 == 0 || (cwpBayFrom != null && cwpBayFrom.getBayNo() % 2 == 0)) { // 去掉 && cwpBayFirst.getHatchId().equals(cwpBayFrom.getHatchId())
                    cwpBayFirst = cwpBayFirst.getBayNo() % 2 != 0 ? cwpBayFrom : cwpBayFirst;
                    CWPBay cwpBayX = PublicMethod.getSteppingBayInHatch(cwpBayFirst, cwpData, CWPDomain.L);
                    if (cwpBayX != null && cwpBayX.getDpCurrentTotalWorkTime() > 0) {
                        cwpCrane.getDpCurCanSelectBays().addFirst(cwpBayX.getBayNo());
                    }
                }
                CWPBay cwpBayLast = cwpData.getCWPBayByBayNo(cwpCrane.getDpFirstCanSelectBays().getLast());
                CWPBay cwpBayTo = cwpData.getCWPBayByBayNo(cwpCrane.getDpWorkBayNoTo());
                if (cwpBayLast.getBayNo() % 2 == 0 || (cwpBayTo != null && cwpBayTo.getBayNo() % 2 == 0)) { // 去掉 && cwpBayLast.getHatchId().equals(cwpBayTo.getHatchId())
                    cwpBayLast = cwpBayLast.getBayNo() % 2 != 0 ? cwpBayTo : cwpBayLast;
                    CWPBay cwpBayX = PublicMethod.getSteppingBayInHatch(cwpBayLast, cwpData, CWPDomain.R);
                    if (cwpBayX != null && cwpBayX.getDpCurrentTotalWorkTime() > 0) {
                        cwpCrane.getDpCurCanSelectBays().addLast(cwpBayX.getBayNo());
                    }
                }
            }
        }
    }


    private void analyzeCraneWaitNotWork(List<CWPCrane> cwpCraneList, CwpData cwpData) {
        //桥机是否需要等待（dpWait = false && canNotWork）：
        for (CWPCrane cwpCrane : cwpCraneList) {
            cwpCrane.setDpWait(false);
        }
        //桥机自己范围的作业量做完
        for (CWPCrane cwpCrane : cwpCraneList) {
            boolean workDone = true;
            for (Integer bayNo : cwpCrane.getDpCurCanSelectBays()) {
                CWPBay cwpBay = cwpData.getCWPBayByBayNo(bayNo);
                if (cwpBay.getDpCurrentTotalWorkTime() > 0) {
                    workDone = false;
                }
            }
            if (workDone && PublicMethod.getNextCrane(cwpCrane, cwpCraneList) == null) { // 最右边的桥机，右边还有作业量，则不能等待
                for (CWPBay cwpBay : cwpData.getAllCWPBays()) {
                    if (cwpBay.getWorkPosition() > cwpCrane.getDpCurrentWorkPosition() && cwpBay.getDpAvailableWorkTime() > 0) {
                        workDone = false;
                    }
                }
            }
            if (workDone && PublicMethod.getFrontCrane(cwpCrane, cwpCraneList) == null) { // 最左边的桥机，左边还有作业量，则不能等待
                for (CWPBay cwpBay : cwpData.getAllCWPBays()) {
                    if (cwpBay.getWorkPosition() < cwpCrane.getDpCurrentWorkPosition() && cwpBay.getDpAvailableWorkTime() > 0) {
                        workDone = false;
                    }
                }
            }
            // 中间桥机，且两边安全距离内大倍位有作业量，则不能等待
            if (workDone && PublicMethod.getNextCrane(cwpCrane, cwpCraneList) != null && PublicMethod.getFrontCrane(cwpCrane, cwpCraneList) != null) {
                for (CWPBay cwpBay : cwpData.getAllCWPBays()) {
                    if (cwpBay.getBayNo() % 2 == 0 && cwpBay.getDpAvailableWorkTime() > 0) {
                        double distance = Math.abs(CalculateUtil.sub(cwpBay.getWorkPosition(), cwpCrane.getDpCurrentWorkPosition()));
                        if (distance < 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance()) {
                            workDone = false;
                        }
                    }
                }
            }
            cwpCrane.setDpWait(workDone);
        }
        // 桥机优先作业垫脚、会形成新的重点路，导致旁边桥机上次作业的倍位不能继续作业（又没有其它合适作业的倍位）必须等待
        for (CWPCrane cwpCrane : cwpCraneList) {
            if (!cwpCrane.getDpWait() && cwpCraneList.size() > 1) { // 只有一部桥机时，不需要处理
                for (Integer bayNo : cwpCrane.getDpCurCanSelectBays()) {
                    CWPBay cwpBay = cwpData.getCWPBayByBayNo(bayNo);
                    // 桥机选择小倍位、且有可作业量的前提下：
                    // 1、如果形成重点路，则必须作业该小倍位，判断由哪部桥机作业；todo :如果没有重点路，则具体要看旁边隔舱倍位是否有小倍位量可以同时作业，来判断是否作业该小倍位。
                    // 2、如果大倍位可作业量为0，则必须作业该小倍位，判断由哪部桥机作业。
                    List<CWPBay> maxCwpBayList = PublicMethod.getMaxWorkTimeCWPBayList(cwpData.getAllCWPBays(), cwpData);
                    long maxWt = PublicMethod.getCurTotalWorkTime(maxCwpBayList);
                    if (PublicMethod.isLittleBay(cwpBay, cwpData) && cwpBay.getDpAvailableWorkTime() > 0) {
                        CWPBay cwpBayD = cwpData.getCWPBayByBayNo(cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNoD());
                        // 判断小倍位所在作业路量是否接近量最大的作业路：接近，则小倍位必须作业；没有接近但是大倍位可作业量为0，即小倍位不作业则大倍位不能作业
                        String side = PublicMethod.getSideByLittleBay(cwpBay, cwpData);
                        List<CWPBay> sideCwpBayList = PublicMethod.getSideCwpBayListInSafeSpan(side, cwpBayD, cwpData); // 以大倍位为起始位置
                        long sideWt = PublicMethod.getCurTotalWorkTime(sideCwpBayList);
                        boolean keyRoadCondition = maxWt - sideWt < 0; // 小倍位形成的作业路与量最大的作业路接近
                        // 1.1、如果小倍位是在上次作业倍位安全距离内，则就算形成重点路也不要去作业
                        Integer lastSelectBayNo = PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult());
                        boolean condition1 = true;
                        if (lastSelectBayNo != null) {
                            condition1 = !PublicMethod.safeSpanBay(lastSelectBayNo, cwpBay.getBayNo(), cwpData);
                        }
                        // 1.2、如果小倍位在重点路上，且重点路上次已经被其它桥机作业了，也不应该去作业
                        if (PublicMethod.inCwpBayList(cwpBay, maxCwpBayList)) {
                            for (CWPBay cwpBay1 : maxCwpBayList) {
                                String lastCraneNo = PublicMethod.getSelectCraneNoInDpResult(cwpBay1.getBayNo(), cwpData.getDpResult());
                                if (lastCraneNo != null && !lastCraneNo.equals(cwpCrane.getCraneNo())) {
                                    condition1 = false;
                                    break;
                                }
                            }
                        }
                        condition1 = keyRoadCondition && condition1;
                        // 2、如果大倍位可作业量为0，且小倍位跟上次作业的倍位是同一个舱的
                        boolean condition2 = false;
                        if (lastSelectBayNo != null) {
                            CWPBay lastCwpBay = cwpData.getCWPBayByBayNo(lastSelectBayNo);
                            condition2 = cwpBayD.getDpAvailableWorkTime() == 0 && lastCwpBay.getHatchId().equals(cwpBay.getHatchId());
                        }
                        if (condition1 || condition2) {
                            List<CWPCrane> sideCwpCraneList = PublicMethod.getSideCwpCraneList(side, cwpCrane, cwpCraneList);
                            List<CWPCrane> sideEffectedCraneList = getEffectedCraneList(side, sideCwpCraneList, cwpBay, cwpData);
                            String otherSide = CWPDomain.L.equals(side) ? CWPDomain.R : CWPDomain.L;
                            List<CWPCrane> selfCwpCraneList = PublicMethod.getSideCwpCraneList(otherSide, cwpCrane, cwpCraneList);
                            selfCwpCraneList.add(cwpCrane);
                            PublicMethod.sortCwpCraneByCraneSeq(selfCwpCraneList);
                            List<CWPCrane> selfEffectedCraneList = getEffectedCraneList(otherSide, selfCwpCraneList, cwpBay, cwpData);
                            if (sideEffectedCraneList.size() > selfEffectedCraneList.size()) { // 旁边受影响的桥机数目要多一些，则由旁边的桥机来作业该小倍位
                                CWPCrane sideCwpCrane = CWPDomain.L.equals(side) ? sideCwpCraneList.get(sideCwpCraneList.size() - 1) : sideCwpCraneList.get(0);
                                setSelectedBayAndCraneWait(sideCwpCrane, cwpBay, selfEffectedCraneList, cwpData);
                            } else {
                                CWPCrane selfCwpCrane = CWPDomain.L.equals(side) ? selfCwpCraneList.get(0) : selfCwpCraneList.get(selfCwpCraneList.size() - 1);
                                setSelectedBayAndCraneWait(selfCwpCrane, cwpBay, sideEffectedCraneList, cwpData);
                            }
                        }
                    }
                    // 如果量最大的作业路接近船期，则需要
                }
            }
        }
        // 如果桥机当前作业的舱位，还有少量小倍位的量，则也算到可作业范围内
        for (CWPCrane cwpCrane : cwpCraneList) {
            if (cwpCrane.getDpCurrentWorkBayNo() != null) {
                CWPBay cwpBay = cwpData.getCWPBayByBayNo(cwpCrane.getDpCurrentWorkBayNo());
                for (Integer bayNoX : cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNos()) {
                    CWPBay cwpBayX = cwpData.getCWPBayByBayNo(bayNoX);
                    if (cwpBayX != null && cwpBayX.getDpAvailableWorkTime() > 0 && !cwpCrane.getDpCurCanSelectBays().contains(bayNoX)) { // 有可作业量、且DpCurCanSelectBays列表中不存在
                        if (cwpBayX.getWorkPosition().compareTo(cwpBay.getWorkPosition()) > 0) {
                            cwpCrane.getDpCurCanSelectBays().addLast(bayNoX);
                        } else {
                            cwpCrane.getDpCurCanSelectBays().addFirst(bayNoX);
                        }
                    }
                }
            }
        }
    }

    private void setSelectedBayAndCraneWait(CWPCrane sideCwpCrane, CWPBay cwpBay, List<CWPCrane> selfEffectedCraneList, CwpData cwpData) {
        cwpBay.setDpSelectedByCraneNo(sideCwpCrane.getCraneNo());
        // 如果小倍位量大于垫脚量阈值，则桥机不需要设置等待，且下一次决策以此为基础继续进行
        long steppingCntT = CWPDefaultValue.steppingCntMoveNum * cwpData.getWorkingData().getCwpConfig().getSingle20FootPadTimeL();
        if (cwpBay.getDpAvailableWorkTime() < steppingCntT) {
            for (CWPCrane cwpCrane1 : selfEffectedCraneList) {
                cwpCrane1.setDpWait(true);
            }
        } else {
            cwpBay.setDpSelectedByCraneTrue(CWPDomain.YES);
            // todo: 不需要等待的桥机可以作业哪些倍位
        }
    }

    private List<CWPCrane> getEffectedCraneList(String side, List<CWPCrane> cwpCraneList, CWPBay cwpBay, CwpData cwpData) {
        List<CWPCrane> craneList = new ArrayList<>();
        double safeSpan = cwpData.getStructureData().getVMHatchSpan() * 2;
        for (int i = 0; i < cwpCraneList.size(); i++) {
            CWPCrane cwpCrane = cwpCraneList.get(i);
            Integer bayNoLast = PublicMethod.getCurBayNoInCranePosition(cwpCrane.getCraneNo(), cwpData.getDpResult().getDpCranePosition());
            CWPBay cwpBayLast = cwpData.getCWPBayByBayNo(bayNoLast);
            if (cwpBayLast != null) {
                double distance = Math.abs(CalculateUtil.sub(cwpBayLast.getWorkPosition(), cwpBay.getWorkPosition()));
                int num = CWPDomain.R.equals(side) ? i + 1 : cwpCraneList.size() - i;
                if (distance < safeSpan * num) {
                    craneList.add(cwpCrane);
                } else if (distance == safeSpan * num) { // 如果正好在隔舱安全距离上，则判断上次作业的倍是否完成，且大倍位是否还有作业量
                    if (cwpBayLast.getDpAvailableWorkTime() == 0) {
                        CWPBay cwpBayD = cwpData.getCWPBayByBayNo(cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNoD());
                        if (cwpBayD != null && cwpBayD.getDpCurrentTotalWorkTime() > cwpBay.getDpAvailableWorkTime()) {
                            craneList.add(cwpCrane);
                        }
                    }
                }
            }
        }
        return craneList;
    }

    private Long getDividedWorkTime(long wt, CWPBay cwpBay, CwpData cwpData) {
        if (cwpBay.getDpAvailableDiscWtD() > 0 && Math.abs(cwpBay.getDpAvailableDiscWtD() - wt) < 3600) {
            return cwpBay.getDpAvailableDiscWtD();
        }
        return null;
    }

    private boolean craneSplitBay(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        Long hatchId1 = cwpData.getCWPBayByBayNo(cwpCrane.getDpCurCanSelectBays().getFirst()).getHatchId();
        if (cwpData.getStructureData().getVMHatchByHatchId(hatchId1).getAllBayNos().contains(cwpBay.getBayNo())) {
            return true;
        }
        Long hatchId2 = cwpData.getCWPBayByBayNo(cwpCrane.getDpCurCanSelectBays().getLast()).getHatchId();
        return cwpData.getStructureData().getVMHatchByHatchId(hatchId2).getAllBayNos().contains(cwpBay.getBayNo());
    }

    private boolean isDividedBay(CWPCrane cwpCrane, CWPBay cwpBay, List<CWPCrane> cwpCraneList) {
        if (cwpBay.getBayNo().equals(cwpCrane.getDpWorkBayNoFrom())) {
            CWPCrane frontCrane = PublicMethod.getFrontCrane(cwpCrane, cwpCraneList);
            if (frontCrane != null && cwpBay.getBayNo().equals(frontCrane.getDpWorkBayNoTo()) && frontCrane.getDpWorkTimeTo() > 144) {
                return true;
            }
        }
        if (cwpBay.getBayNo().equals(cwpCrane.getDpWorkBayNoTo())) {
            CWPCrane nextCrane = PublicMethod.getNextCrane(cwpCrane, cwpCraneList);
            if (nextCrane != null && cwpBay.getBayNo().equals(nextCrane.getDpWorkBayNoFrom()) && nextCrane.getDpWorkTimeFrom() > 144) {
                return true;
            }
        }
        return false;
    }
}
