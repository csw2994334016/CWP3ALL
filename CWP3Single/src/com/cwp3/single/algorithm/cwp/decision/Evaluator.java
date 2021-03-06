package com.cwp3.single.algorithm.cwp.decision;

import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkBlock;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.cwp.method.PublicMethod;
import com.cwp3.single.algorithm.cwp.modal.*;
import com.cwp3.single.data.CwpData;
import com.cwp3.utils.CalculateUtil;

import java.util.*;

/**
 * Created by csw on 2018/6/13.
 * Description:
 */
public class Evaluator {

    public List<DPBranch> getFirstDpBranchList(CwpData cwpData) {
        List<DPBranch> dpBranchList = new ArrayList<>();
        List<CWPBay> cwpBays = cwpData.getAllCWPBays();
        for (Map.Entry<String, List<CWPCrane>> entry : cwpData.getDpFirstCwpCraneMap().entrySet()) {
            List<CWPCrane> cwpCranes = entry.getValue();

            int n = 1;
            for (CWPCrane cwpCrane : cwpCranes) {
                n *= cwpCrane.getDpSelectBays().size() > 0 ? cwpCrane.getDpSelectBays().size() : 1;
            }
            cwpData.getWorkingData().getLogger().logDebug("所有分支：" + n);

            List<List<DPPair>> dpPairLists = new ArrayList<>();

            CWPCrane cwpCrane1 = cwpCranes.get(0);
            if (cwpCrane1.getFirstWorkBayNo() != null) {
                List<DPPair> dpPairList1 = new ArrayList<>();
                DPPair dpPair1 = new DPPair<>(cwpCrane1.getCraneNo(), cwpCrane1.getFirstWorkBayNo());
                dpPairList1.add(dpPair1);
                dpPairLists.add(dpPairList1);
            } else {
                for (Integer bayNo : cwpCrane1.getDpSelectBays()) {
                    List<DPPair> dpPairList1 = new ArrayList<>();
                    DPPair dpPair1 = new DPPair<>(cwpCrane1.getCraneNo(), bayNo);
                    dpPairList1.add(dpPair1);
                    dpPairLists.add(dpPairList1);
                }
            }
            for (int i = 1; i < cwpCranes.size(); i++) {
                CWPCrane cwpCrane = cwpCranes.get(i);
                List<Integer> bayNoList = new ArrayList<>();
                if (cwpCrane.getFirstWorkBayNo() != null) {
                    bayNoList.add(cwpCrane.getFirstWorkBayNo());
                } else {
                    bayNoList.addAll(cwpCrane.getDpSelectBays());
                }
                List<List<DPPair>> tempDpPairLists = new ArrayList<>();
                for (List<DPPair> dpPairList : dpPairLists) {
                    for (Integer bayNo : bayNoList) {
                        Integer bayNo1 = (Integer) dpPairList.get(dpPairList.size() - 1).getSecond();
                        if (!PublicMethod.safeSpanBay(bayNo, bayNo1, cwpData)) {
                            List<DPPair> tempDpPairList = PublicMethod.copyDpPairList(dpPairList);
                            DPPair dpPair = new DPPair<>(cwpCrane.getCraneNo(), bayNo);
                            tempDpPairList.add(dpPair);
                            tempDpPairLists.add(tempDpPairList);
                        }
                    }
                }
                if (tempDpPairLists.size() > 0) {
                    dpPairLists = tempDpPairLists;
                }
            }

            // 根据上手装卸参数设置，缩减分支

            // 如果有桥机第一次选择作业小倍位的单边/垫脚箱，则认为需要旁边桥机的等待

            //1、缩减分支，预演推算由于开路舱位，导致后续：桥机等待、产生安全距离的情况

            cwpData.getWorkingData().getLogger().logDebug("缩减后分支：" + dpPairLists.size());

            List<DPCraneSelectBay> dpCraneSelectBayList = new ArrayList<>();
            for (CWPCrane cwpCrane : cwpCranes) {
                for (CWPBay cwpBay : cwpBays) {
                    DPCraneSelectBay dpCraneSelectBay = createDPCraneSelectBay(cwpCrane, cwpBay);
                    if (cwpBay.getDpAvailableWorkTime() > 0) {
                        if (cwpCrane.getDpCurCanSelectBays().contains(cwpBay.getBayNo())) {
                            dpCraneSelectBay.setDpFeature(new DPFeature(CWPDesc.inWorkRange.getCode(), CWPDesc.inWorkRange.getDesc()));
                        } else {
                            dpCraneSelectBay.setDpFeature(new DPFeature(CWPDesc.outWorkRange.getCode(), CWPDesc.outWorkRange.getDesc()));
                        }
                    } else {
                        dpCraneSelectBay.setDpFeature(new DPFeature(CWPDesc.canNotWork.getCode(), CWPDesc.canNotWork.getDesc()));
                    }
                    dpCraneSelectBayList.add(dpCraneSelectBay);
                }
            }

            if (dpPairLists.size() == 0 || cwpData.getWorkingData().getCwpConfig().getLoadPrior() == null) { // 默认分支
                DPBranch defaultDpBranch = new DPBranch();
                List<CWPCrane> cwpCraneList = PublicMethod.copyCwpCraneList(cwpCranes);
                defaultDpBranch.setDpStrategyType(entry.getKey());
                defaultDpBranch.setDpCwpCraneList(cwpCraneList);
                defaultDpBranch.getDpCraneSelectBays().addAll(dpCraneSelectBayList);
                dpBranchList.add(defaultDpBranch);
            }

            for (List<DPPair> dpPairList : dpPairLists) {
                DPBranch dpBranch = new DPBranch();
                for (DPCraneSelectBay dpCraneSelectBay : dpCraneSelectBayList) {
                    if (PublicMethod.inDpPairList(dpCraneSelectBay.getDpPair(), dpPairList)) {
                        DPCraneSelectBay dpCraneSelectBay1 = new DPCraneSelectBay(dpCraneSelectBay.getDpPair());
                        dpCraneSelectBay1.setDpDistance(dpCraneSelectBay.getDpDistance());
                        dpCraneSelectBay1.setDpWorkTime(dpCraneSelectBay.getDpWorkTime());
                        dpCraneSelectBay1.setDpFeature(new DPFeature(CWPDesc.firstSelectFactor.getCode(), CWPDesc.firstSelectFactor.getDesc()));
                        dpBranch.getDpCraneSelectBays().add(dpCraneSelectBay1);
                    } else {
                        dpBranch.getDpCraneSelectBays().add(dpCraneSelectBay);
                    }
                }
                for (DPCraneSelectBay dpCraneSelectBay : dpBranch.getDpCraneSelectBays()) {
                    if (dpCraneSelectBay.getDpFeature().getCode() >= 2) {
                        CWPCrane cwpCrane11 = PublicMethod.getCwpCraneByCraneNo((String) dpCraneSelectBay.getDpPair().getFirst(), cwpCranes);
                        CWPBay cwpBay1 = cwpData.getCWPBayByBayNo((Integer) dpCraneSelectBay.getDpPair().getSecond());
                        if (hatchBayDelay(cwpCrane11, cwpBay1, cwpData)) {
                            // 第一次决策，如果倍位作业需要推迟，对应该舱其它倍位作业量需要提升一个优先级
                            if (dpCraneSelectBay.getDpFeature().getDesc().equals(CWPDesc.firstSelectFactor.getDesc())) {
                                dpCraneSelectBay.setDpFeature(new DPFeature(CWPDesc.inWorkRange.getCode(), CWPDesc.inWorkRange.getDesc()));
                                // 遍历该舱的其它倍位
                                for (Integer bayNo : cwpData.getStructureData().getVMHatchByHatchId(cwpBay1.getHatchId()).getAllBayNos()) {
                                    if (!bayNo.equals(cwpBay1.getBayNo())) {
                                        DPCraneSelectBay dpCraneSelectBay1 = DPCraneSelectBay.getDpCraneSelectBayByPair(dpBranch.getDpCraneSelectBays(), new DPPair<>((String) dpCraneSelectBay.getDpPair().getFirst(), bayNo));
                                        if (dpCraneSelectBay1 != null && dpCraneSelectBay1.getDpFeature().getCode() >= 2) {
                                            dpCraneSelectBay1.setDpFeature(new DPFeature(CWPDesc.hatchBayFirst.getCode(), CWPDesc.hatchBayFirst.getDesc()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                List<CWPCrane> cwpCraneList1 = PublicMethod.copyCwpCraneList(cwpCranes);
                dpBranch.setDpStrategyType(entry.getKey());
                dpBranch.setDpCwpCraneList(cwpCraneList1);
                dpBranchList.add(dpBranch);
            }
        }
        cwpData.getWorkingData().getLogger().logInfo("Branch number：" + dpBranchList.size());
        return dpBranchList;
    }

    public List<DPBranch> getCurDpBranchList(CwpData cwpData) {
        List<DPCraneSelectBay> dpCraneSelectBayList = getCurDpCraneSelectBayList(cwpData);
        List<DPBranch> dpBranchList = new ArrayList<>();
        List<DPCraneSelectBay> dpCraneSelectBays1 = new ArrayList<>();
        for (DPCraneSelectBay dpCraneSelectBay : dpCraneSelectBayList) {
            dpCraneSelectBay.setDpFeature(dpCraneSelectBay.getDpFeatureList().get(0));
            if (dpCraneSelectBay.getDpFeatureList().size() > 1) {
                DPCraneSelectBay dpCraneSelectBay1 = dpCraneSelectBay.deepCopy();
                dpCraneSelectBay1.setDpFeature(dpCraneSelectBay.getDpFeatureList().get(1));
                dpCraneSelectBays1.add(dpCraneSelectBay1);
            }
        }
        if (dpCraneSelectBays1.size() > 0) { // 桥机选择倍位特征值有多个可能性，每个可能性一个分支
            for (DPCraneSelectBay dpCraneSelectBay1 : dpCraneSelectBays1) {
                DPBranch dpBranch = new DPBranch();
                List<CWPCrane> cwpCraneList1 = PublicMethod.copyCwpCraneList(cwpData.getDpCwpCraneList());
                dpBranch.setDpStrategyType(cwpData.getDpStrategyType());
                dpBranch.setDpCwpCraneList(cwpCraneList1);
                for (DPCraneSelectBay dpCraneSelectBay : dpCraneSelectBayList) {
                    if (dpCraneSelectBay.equalsWithPair(dpCraneSelectBay1.getDpPair())) {
                        dpBranch.getDpCraneSelectBays().add(dpCraneSelectBay1);
                    } else {
                        dpBranch.getDpCraneSelectBays().add(dpCraneSelectBay);
                    }
                }
                dpBranchList.add(dpBranch);
            }
        } else {
            DPBranch dpBranch = new DPBranch();
            List<CWPCrane> cwpCraneList1 = PublicMethod.copyCwpCraneList(cwpData.getDpCwpCraneList());
            dpBranch.setDpStrategyType(cwpData.getDpStrategyType());
            dpBranch.setDpCwpCraneList(cwpCraneList1);
            for (DPCraneSelectBay dpCraneSelectBay : dpCraneSelectBayList) {
                dpBranch.getDpCraneSelectBays().add(dpCraneSelectBay);
            }
            dpBranchList.add(dpBranch);
        }
        cwpData.getWorkingData().getLogger().logInfo("Branch number：" + dpBranchList.size());
        return dpBranchList;
    }

    public DPBranch getCurDpBranch(CwpData cwpData) {
        List<DPCraneSelectBay> dpCraneSelectBayList = getCurDpCraneSelectBayList(cwpData);
        DPBranch dpBranch = new DPBranch();
        List<CWPCrane> cwpCraneList1 = PublicMethod.copyCwpCraneList(cwpData.getDpCwpCraneList());
        dpBranch.setDpStrategyType(cwpData.getDpStrategyType());
        dpBranch.setDpCwpCraneList(cwpCraneList1);
        for (DPCraneSelectBay dpCraneSelectBay : dpCraneSelectBayList) {
            dpCraneSelectBay.setDpFeature(dpCraneSelectBay.getDpFeatureList().get(0));
            dpBranch.getDpCraneSelectBays().add(dpCraneSelectBay);
        }
        return dpBranch;
    }

    private List<DPCraneSelectBay> getCurDpCraneSelectBayList(CwpData cwpData) {
        List<CWPCrane> cwpCranes = cwpData.getDpCwpCraneList();
        List<CWPBay> cwpBays = cwpData.getAllCWPBays();
        List<DPCraneSelectBay> dpCraneSelectBayList = new ArrayList<>();
        for (CWPCrane cwpCrane : cwpCranes) {
            for (CWPBay cwpBay : cwpBays) {
                DPCraneSelectBay dpCraneSelectBay = createDPCraneSelectBay(cwpCrane, cwpBay);
                dpCraneSelectBay.setTroughMachine(PublicMethod.craneThroughMachine(cwpCrane, cwpBay, cwpData));
                if (cwpBay.getDpAvailableWorkTime() > 0 && !cwpCrane.getDpWait()) {
                    if (craneCanSelectBay(cwpCrane, cwpBay, cwpData)) {
                        DPFeature dpFeature = new DPFeature(CWPDesc.inWorkRange.getCode(), CWPDesc.inWorkRange.getDesc());
                        if (hatchBayDelay(cwpCrane, cwpBay, cwpData)) {
                            dpFeature = new DPFeature(CWPDesc.hatchBayDelay.getCode(), CWPDesc.hatchBayDelay.getDesc());
                        }
                        if (splitRoad(cwpCrane, cwpBay, cwpData)) {
                            dpFeature = new DPFeature(CWPDesc.splitRoad.getCode(), CWPDesc.splitRoad.getDesc());
                        }
                        if (specialBay(cwpCrane, cwpBay, cwpData)) {
                            dpFeature = new DPFeature(CWPDesc.specialBay.getCode(), CWPDesc.specialBay.getDesc());
                        }
                        if (lastSelectHatch(cwpCrane, cwpBay, cwpData)) {
                            dpFeature = new DPFeature(CWPDesc.lastSelectHatch.getCode(), CWPDesc.lastSelectHatch.getDesc());
                        }
                        if (steppingCntFirst(cwpCrane, cwpBay, cwpData)) {
                            dpFeature = new DPFeature(CWPDesc.steppingCntFirst.getCode(), CWPDesc.steppingCntFirst.getDesc());
                        }
                        if (cwpBay.getReStowCntTimeL() > 0) {
                            dpFeature = new DPFeature(CWPDesc.reStowCntDelay.getCode(), CWPDesc.reStowCntDelay.getDesc());
                        }
                        dpCraneSelectBay.getDpFeatureList().add(dpFeature);
                        // 开分支考虑，桥机选择倍位第二特征
                        if (craneDividedBay(cwpCrane, cwpBay, dpFeature, cwpData)) {
                            dpCraneSelectBay.getDpFeatureList().add(new DPFeature(CWPDesc.preAvoidKeyRoad.getCode(), CWPDesc.preAvoidKeyRoad.getDesc()));
                        }
                    } else {
                        dpCraneSelectBay.getDpFeatureList().add(new DPFeature(CWPDesc.outWorkRange.getCode(), CWPDesc.outWorkRange.getDesc()));
                    }
                } else {
                    dpCraneSelectBay.getDpFeatureList().add(new DPFeature(CWPDesc.canNotWork.getCode(), CWPDesc.canNotWork.getDesc()));
                }
                dpCraneSelectBayList.add(dpCraneSelectBay);
            }
        }
        // 倍位必须由哪部桥机选择
        for (CWPBay cwpBay : cwpBays) {
            if (cwpBay.getDpSelectedByCraneNo() != null) {
                DPCraneSelectBay dpCraneSelectBay = DPCraneSelectBay.getDpCraneSelectBayByPair(dpCraneSelectBayList, new DPPair<>(cwpBay.getDpSelectedByCraneNo(), cwpBay.getBayNo()));
                if (dpCraneSelectBay != null) {
                    dpCraneSelectBay.getDpFeatureList().clear();
                    dpCraneSelectBay.getDpFeatureList().add(new DPFeature(CWPDesc.mustSelectByCrane.getCode(), CWPDesc.mustSelectByCrane.getDesc()));
                }
            }
            // 如果倍位有被锁定的桥机号，则该倍位不能被其它桥机选择
            if (cwpBay.getDpLockByCraneNo() != null) {
                for (CWPCrane cwpCrane : cwpCranes) {
                    if (!cwpCrane.getCraneNo().equals(cwpBay.getDpLockByCraneNo())) {
                        DPCraneSelectBay dpCraneSelectBay = DPCraneSelectBay.getDpCraneSelectBayByPair(dpCraneSelectBayList, new DPPair<>(cwpCrane.getCraneNo(), cwpBay.getBayNo()));
                        if (dpCraneSelectBay != null && dpCraneSelectBay.getDpFeatureList().get(0).getCode() > 1) {
                            dpCraneSelectBay.getDpFeatureList().clear();
                            dpCraneSelectBay.getDpFeatureList().add(new DPFeature(CWPDesc.nonLockBayCrane.getCode(), CWPDesc.nonLockBayCrane.getDesc()));
                        }
                    }
                }
            }
        }
        // 人工锁定的桥机作业块与桥机当前累计作业进行对比，是不是到达桥机锁定作业块的倍位
        for (CWPCrane cwpCrane : cwpCranes) {
            List<WorkBlock> workBlockList = cwpData.getWorkingData().getLockCraneWorkBlockMap().get(cwpCrane.getCraneNo());
            if (workBlockList != null && workBlockList.size() > 0) {
                Collections.sort(workBlockList, new Comparator<WorkBlock>() {
                    @Override
                    public int compare(WorkBlock o1, WorkBlock o2) {
                        return o1.getCraneSeq().compareTo(o2.getCraneSeq());
                    }
                });
                int n = 0;
                for (WorkBlock workBlock : workBlockList) {
                    if (cwpCrane.getDpWorkCntAmount() >= n && cwpCrane.getDpWorkCntAmount() < n + workBlock.getPlanAmount()) {
                        DPCraneSelectBay dpCraneSelectBay = DPCraneSelectBay.getDpCraneSelectBayByPair(dpCraneSelectBayList, new DPPair<>(cwpCrane.getCraneNo(), Integer.valueOf(workBlock.getBayNo())));
                        if (dpCraneSelectBay != null) {
                            dpCraneSelectBay.getDpFeatureList().clear();
                            dpCraneSelectBay.getDpFeatureList().add(new DPFeature(CWPDesc.bayLockByCrane.getCode(), CWPDesc.bayLockByCrane.getDesc()));
                        }
                        break;
                    }
                    n += workBlock.getPlanAmount();
                }
            }
        }
        return dpCraneSelectBayList;
    }


    private DPCraneSelectBay createDPCraneSelectBay(CWPCrane cwpCrane, CWPBay cwpBay) {
        DPCraneSelectBay dpCraneSelectBay = new DPCraneSelectBay(new DPPair<>(cwpCrane.getCraneNo(), cwpBay.getBayNo()));
        dpCraneSelectBay.setDpDistance(Math.abs(cwpCrane.getDpCurrentWorkPosition() - cwpBay.getWorkPosition()));
        dpCraneSelectBay.setDpWorkTime(cwpBay.getDpAvailableWorkTime());
        return dpCraneSelectBay;
    }

    private boolean craneCanSelectBay(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        boolean canSelect = false;
        //1.1：桥机平均作业量范围内的倍位
        if (cwpCrane.getDpCurCanSelectBays().contains(cwpBay.getBayNo())) {
            canSelect = true;
        }
        return canSelect;
    }

    private boolean hatchBayDelay(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        Integer bayNoD = cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNoD();
        // 同一个舱内大倍位置推迟
        if (cwpBay.getBayNo().equals(bayNoD)) { // 需要判断是否推迟作业的是大倍位
            for (Integer bayNoX : cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNos()) {
                if (!cwpBay.getBayNo().equals(bayNoX)) {
                    CWPBay cwpBayX = cwpData.getCWPBayByBayNo(bayNoX);
                    // 如果小倍位有可作业量、且可以一次性做完，则大倍位推迟作业
                    if (cwpBayX.getDpAvailableWorkTime() > 0 && cwpBayX.getDpAvailableWorkTime().equals(cwpBayX.getDpCurrentTotalWorkTime())) {
                        return true;
                    }
                    // 如果小倍位卸船能一次性做完、且大倍位有可作业量，则大倍位推迟作业
                    if (cwpBayX.getDpAvailableWorkTime() > 0 && cwpBayX.getDpAvailableWorkTime().equals(cwpBayX.getDpAvailableDiscWtX()) && cwpBay.getDpAvailableWorkTime() > 0) {
                        return true;
                    }
                    // 如果大倍位有装船量，且小倍位有卸船量（避免出现单边箱没卸，开始装大倍位箱子），则大倍位推迟作业
                    if (cwpBay.getDpAvailableLoadWtD() > 0 && cwpBayX.getDpAvailableDiscWtX() > 0) {
                        return true;
                    }
                }
            }
        } else { // 同一舱内小倍位置推迟，一般是垫脚推迟，等到一次性能做完时选择作业
            return cwpBay.getDpSteppingAvailableWt() > 0 && !cwpBay.getDpSteppingAvailableWt().equals(cwpBay.getDpSteppingTotalWt());
        }
        return false;
    }

    private boolean splitRoad(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        if (CWPDomain.YES.equals(cwpData.getWorkingData().getCwpConfig().getSplitRoad())) {
            Integer bayNoLast = PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult());
            if (bayNoLast != null) {
                CWPBay cwpBayLast = cwpData.getCWPBayByBayNo(bayNoLast);
                boolean flag = false;
                if (!cwpCrane.getDpCurCanSelectBays().contains(bayNoLast)) { // 1、上次作业在分割倍，且已完成分割量
                    double d1 = CalculateUtil.sub(cwpData.getCWPBayByBayNo(cwpCrane.getDpCurCanSelectBays().getFirst()).getWorkPosition(), cwpBayLast.getWorkPosition());
                    double d2 = CalculateUtil.sub(cwpBayLast.getWorkPosition(), cwpData.getCWPBayByBayNo(cwpCrane.getDpCurCanSelectBays().getLast()).getWorkPosition());
                    flag = (d1 >= 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance()) || (d2 >= 2 * cwpData.getWorkingData().getCwpConfig().getSafeDistance());
                }
                Integer lastBayNoD = cwpData.getStructureData().getVMHatchByHatchId(cwpBayLast.getHatchId()).getBayNoD();
                if (cwpData.getCwpBayListByBayNoD(lastBayNoD) == null || flag) { // 2、上次作业的舱做完了
                    Integer bayNoD = cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNoD();
                    if (cwpBay.getBayNo().equals(bayNoD)) {
                        if (cwpCrane.getDpCurCanSelectBays().contains(bayNoD - 4) && cwpCrane.getDpCurCanSelectBays().contains(bayNoD + 4)) {
                            CWPBay cwpBayF = cwpData.getCWPBayByBayNo(bayNoD - 4);
                            CWPBay cwpBayN = cwpData.getCWPBayByBayNo(bayNoD + 4);
                            if (cwpBayF.getDpAvailableWorkTime() > 10 * cwpData.getWorkingData().getCwpConfig().getOneCntTime() && cwpBayN.getDpAvailableWorkTime() > 10 * cwpData.getWorkingData().getCwpConfig().getOneCntTime()) {
                                if (PublicMethod.getNextCrane(cwpCrane, cwpData.getDpCwpCraneList()) == null) { // 最后一部桥机，上次作业倍位之前还有可作用倍位，则不能算劈路
                                    if (cwpBay.getWorkPosition() - cwpBayLast.getWorkPosition() > 0) {
                                        for (Integer bayNo1 : cwpCrane.getDpCurCanSelectBays()) {
                                            CWPBay cwpBay1 = cwpData.getCWPBayByBayNo(bayNo1);
                                            if (cwpBay1.getWorkPosition() - cwpBayLast.getWorkPosition() < 0 && cwpBay1.getDpAvailableWorkTime() > 0) {
                                                return false;
                                            }
                                        }
                                    }
                                } else if (PublicMethod.getFrontCrane(cwpCrane, cwpData.getDpCwpCraneList()) == null) { // 第一部桥机
                                    if (cwpBay.getWorkPosition() - cwpBayLast.getWorkPosition() < 0) {
                                        for (Integer bayNo1 : cwpCrane.getDpCurCanSelectBays()) {
                                            CWPBay cwpBay1 = cwpData.getCWPBayByBayNo(bayNo1);
                                            if (cwpBay1.getWorkPosition() > cwpBayLast.getWorkPosition() && cwpBay1.getDpAvailableWorkTime() > 0) {
                                                return false;
                                            }
                                        }
                                    }
                                } else {
                                    return true;
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean specialBay(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        Integer bayNoLast = PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult());
        if (bayNoLast == null) {
            bayNoLast = PublicMethod.getCurBayNoInCranePosition(cwpCrane.getCraneNo(), cwpData.getDpResult().getDpCranePosition());
        }
        if (bayNoLast != null) {
            CWPBay cwpBayLast = cwpData.getCWPBayByBayNo(bayNoLast);
            CWPBay cwpBayF = cwpData.getCWPBayByBayNo(cwpCrane.getDpCurCanSelectBays().getFirst());
            CWPBay cwpBayL = cwpData.getCWPBayByBayNo(cwpCrane.getDpCurCanSelectBays().getLast());
            CWPBay machineBay = null;
            for (CWPBay machine : cwpData.getAllMachineBays()) {
                if (machine.getWorkPosition() > cwpBayF.getWorkPosition() && machine.getWorkPosition() < cwpBayL.getWorkPosition()) {
                    machineBay = machine;
                    break;
                }
            }
            if (machineBay != null) {
                if ((cwpBayLast.getWorkPosition() > machineBay.getWorkPosition()) && (cwpBay.getWorkPosition() > machineBay.getWorkPosition())) {
                    return true;
                }
                return (cwpBayLast.getWorkPosition() < machineBay.getWorkPosition()) && (cwpBay.getWorkPosition() < machineBay.getWorkPosition());
            }
        }
        return false;
    }

    private boolean craneDividedBay(CWPCrane cwpCrane, CWPBay cwpBay, DPFeature dpFeature, CwpData cwpData) {
        boolean hatchWtCompletedFlag = false;
        Integer bayNoLast = PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult());
        if (bayNoLast != null) {
            CWPBay cwpBayLast = cwpData.getCWPBayByBayNo(bayNoLast);
            Integer bayNoLastD = cwpData.getStructureData().getVMHatchByHatchId(cwpBayLast.getHatchId()).getBayNoD();
            List<CWPBay> cwpBayLastList = cwpData.getCwpHatchBayMap().get(bayNoLastD);
            // 上次舱量已经完成
            if (cwpBayLastList == null) {
                hatchWtCompletedFlag = true;
            }
            if (hatchWtCompletedFlag) {
                List<CWPCrane> cwpCraneList = new ArrayList<>();
                for (CWPCrane cwpCrane1 : cwpData.getDpCwpCraneList()) {
                    if (cwpBay.getBayNo().equals(cwpData.getStructureData().getVMHatchByHatchId(cwpBay.getHatchId()).getBayNoD()) && cwpCrane1.getDpCurCanSelectBays().contains(cwpBay.getBayNo())) {
                        cwpCraneList.add(cwpCrane1);
                    }
                }
                if (cwpCraneList.size() == 2 && cwpCraneList.contains(cwpCrane)) { // 该倍位被两部桥机作业，且其中一部是当前桥机
                    CWPCrane cwpCraneSide = null;
                    for (CWPCrane cwpCrane1 : cwpCraneList) {
                        if (!cwpCrane1.getCraneNo().equals(cwpCrane.getCraneNo())) {
                            cwpCraneSide = cwpCrane1;
                        }
                    }
                    if (cwpCraneSide != null) {
                        // 不能跨越驾驶台/烟囱选择该倍位
                        return !dpFeature.getDesc().equals(CWPDesc.specialBay.getDesc());
                    }
                }
            }
        }
        return false;
    }

    private boolean lastSelectHatch(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        if (cwpData.getDpResult().getDpTraceBack().size() > 0) {
            Integer lastBayNo = PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult());
            if (lastBayNo == null) { //桥机上次未选择倍位作业，则看桥机当前所在的位置
                lastBayNo = PublicMethod.getCurBayNoInCranePosition(cwpCrane.getCraneNo(), cwpData.getDpResult().getDpCranePosition());
            }
            if (lastBayNo != null) {
                CWPBay cwpBayLast = cwpData.getCWPBayByBayNo(lastBayNo);
                if (cwpBayLast.getDpAvailableWorkTime() > 0) {
                    return cwpBay.getBayNo().equals(cwpBayLast.getBayNo());
                } else {
                    return cwpBay.getHatchId().equals(cwpBayLast.getHatchId());
                }
            }
        }
        return false;
    }

    private boolean steppingCntFirst(CWPCrane cwpCrane, CWPBay cwpBay, CwpData cwpData) {
        if (cwpBay.getDpSteppingCntFlag()) { // 垫脚可以一次性做完，从大倍位置上移过来作业垫脚
            Integer bayNo = PublicMethod.getSelectBayNoInDpResult(cwpCrane.getCraneNo(), cwpData.getDpResult());
            if (bayNo == null) { // 桥机上次未选择倍位作业，则看桥机当前所停的位置
                bayNo = PublicMethod.getCurBayNoInCranePosition(cwpCrane.getCraneNo(), cwpData.getDpResult().getDpCranePosition());
            }
            if (bayNo != null) { // 垫脚优先作业一定是桥机在上次选择倍位作业的基础上的
                CWPBay cwpBayLast = cwpData.getCWPBayByBayNo(bayNo);
                // 加强判断，如果上次作业的倍位卸船量没有做完，则需要做完剩余的卸船量
                if (cwpBayLast.getDpAvailableDiscWtD() == 0) {
                    return cwpBay.getHatchId().equals(cwpBayLast.getHatchId());
                }
            }
        }
        return false;
    }

    private CWPCrane getFirstUsableCrane(List<CWPCrane> cwpCranes) {
        for (CWPCrane cwpCrane : cwpCranes) {
            if (cwpCrane.getFirstWorkBayNo() == null && cwpCrane.getDpSelectBays().size() > 0) {
                return cwpCrane;
            }
        }
        return null;
    }

    //---------

    public boolean invalidBranch(CwpData cwpData) {
        long vesselTime = cwpData.getVesselTime();
        Map<Integer, List<CWPBay>> everyRoadMap = PublicMethod.getCurEveryRoadBayMap(cwpData.getAllCWPBays(), cwpData);
        for (List<CWPBay> cwpBayList : everyRoadMap.values()) {
            long roadWt = PublicMethod.getCurTotalWorkTime(cwpBayList);
            if (roadWt > vesselTime) {
                cwpData.getWorkingData().getLogger().logDebug("......去掉不能满足船期的分支......");
                cwpData.setDpInvalidateBranch(CWPDomain.INVALIDATE_BRANCH);
                return false;
            }
        }
        return false;
    }

    //---------

    public List<CwpData> getCorrectResult(List<CwpData> cwpDataList) {
        List<CwpData> cwpDataNewList = new ArrayList<>();
        for (CwpData cwpData1 : cwpDataList) {
            boolean correctFlag = true;
            if (CWPDomain.EXCEPTION_BRANCH.equals(cwpData1.getDpExceptionBranch())) {
                correctFlag = false;
            }
            if (correctFlag) {
                Set<WorkMove> workMoveSet = new HashSet<>(cwpData1.getMoveData().getLoadWorkMoveMap().values());
                for (WorkMove workMove : workMoveSet) {
                    if (workMove.getMoveOrder() == null) {
                        correctFlag = false;
                        break;
                    }
                }
                if (correctFlag) {
                    Set<WorkMove> workMoveSet1 = new HashSet<>(cwpData1.getMoveData().getDiscWorkMoveMap().values());
                    for (WorkMove workMove : workMoveSet1) {
                        if (workMove.getMoveOrder() == null) {
                            correctFlag = false;
                            break;
                        }
                    }
                }
            }
            if (correctFlag) {
                cwpDataNewList.add(cwpData1);
            }
        }
        return cwpDataNewList;
    }

    public CwpData getDepthBestResult(List<CwpData> cwpDataList) {
        return cwpDataList.get(0);
    }

    public List<CwpData> getBestResult(List<CwpData> cwpDataList) {
        List<CwpData> resultList = new ArrayList<>();
        List<CwpData> availableCwpDataList = new ArrayList<>();
        for (CwpData cwpData : cwpDataList) {
            if (cwpData.getDpInvalidateBranch() == null) { // 说明是有效的
                availableCwpDataList.add(cwpData);
            }
        }
        if (availableCwpDataList.size() > 0) {
            cwpDataList = availableCwpDataList;
        }
        for (CwpData cwpData : cwpDataList) {
            cwpData.setEvaluateTime(cwpData.getDpCurrentTime() / 600);
        }
        // 将结果按600秒间隔进行分组，组内移动次数最小的作为结果
        Collections.sort(cwpDataList, new Comparator<CwpData>() {
            @Override
            public int compare(CwpData o1, CwpData o2) {
                if (o1.getEvaluateTime().equals(o2.getEvaluateTime())) {
                    return o1.getDpMoveNumber().compareTo(o2.getDpMoveNumber());
                } else {
                    return o1.getEvaluateTime().compareTo(o2.getEvaluateTime());
                }
            }
        });

        for (CwpData cwpData : cwpDataList) {
            cwpData.getWorkingData().getLogger().logDebug("策略：" + cwpData.getDpStrategyType() + ", 移动次数：" + cwpData.getDpMoveNumber() + ", 结束时间：" + cwpData.getDpCurrentTime() + "、" + cwpData.getEvaluateTime());
        }
        resultList.add(cwpDataList.get(0));
        return cwpDataList;
    }
}
