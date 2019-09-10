package com.cwp3.single.algorithm.move.calculator.analysis;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.crane.CMCraneWorkFlow;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.calculator.strategy.*;
import com.cwp3.single.data.MoveData;
import com.cwp3.utils.StringUtil;

import java.util.Map;

/**
 * Created by csw on 2018/9/4.
 * Description: 船舶结构箱量分布分析、返回使用哪种策略进行编序
 */
public class Analyzer {

    public static Strategy getCurStrategy(Long hatchId, Integer bayNo, Map<Integer, WorkMove> rowNoMoveMap, MoveData moveData, WorkingData workingData, StructureData structureData) {
        //参数是否设置了边装边卸策略
//        String key = StringUtil.getKey(StringUtil.getKey(structureData.getVMBayByBayKey(StringUtil.getKey(bayNo, CWPDomain.BOARD_BELOW)).getHatchId(), CWPDomain.BOARD_BELOW), CWPDomain.DL_TYPE_DISC);
//        CMCraneWorkFlow cmCraneWorkFlow = workingData.getCMCraneWorkFlowByKey(key);
        //只做装和卸策略的判断
        String dlType = CWPDomain.DL_TYPE_LOAD;
        for (WorkMove workMove : rowNoMoveMap.values()) {
            if (CWPDomain.DL_TYPE_DISC.equals(workMove.getDlType())) {
                dlType = CWPDomain.DL_TYPE_DISC;
            }
        }
        if (CWPDomain.DL_TYPE_LOAD.equals(dlType)) {
            // 判断是否使用锁定顺序优先作业策略
            if (hasWorkFirst(bayNo, rowNoMoveMap)) {
                return new LoadDiscFirstStrategy(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
            }
            int tierNo = getCurMinTierNo(rowNoMoveMap);
            if (tierNo >= 50) {
                return new LoadStrategyByTier(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
            } else {
                return new LoadStrategy(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
            }
        } else {
            // 判断是否使用锁定顺序优先作业策略
            if (hasWorkFirst(bayNo, rowNoMoveMap)) {
                return new LoadDiscFirstStrategy(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
            }
            int tireNo = getCurMaxTierNo(rowNoMoveMap);
            if (tireNo >= 50) { //甲板上
                if (discByTier(rowNoMoveMap, workingData, structureData)) {
                    return new DiscStrategyBYTier(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
                } else if (discByWf(rowNoMoveMap, workingData, structureData)) {
                    return new DiscStrategyByWf(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
                } else {
                    return new DiscStrategyBYTier(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
                }
            } else {
                return new DiscStrategy(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
            }
        }
    }

    private static int getCurMinTierNo(Map<Integer, WorkMove> rowNoMoveMap) {
        int tierNo = 1000;
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            if (CWPDomain.DL_TYPE_LOAD.equals(entry.getValue().getDlType())) {
                if (entry.getValue().getTierNo() < tierNo) {
                    tierNo = entry.getValue().getTierNo();
                }
            }
        }
        return tierNo;
    }

    private static int getCurMaxTierNo(Map<Integer, WorkMove> rowNoMoveMap) {
        int tierNo = -1; //可作业顶层最大层号
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            if (CWPDomain.DL_TYPE_DISC.equals(entry.getValue().getDlType())) {
                if (entry.getValue().getTierNo() > tierNo) {
                    tierNo = entry.getValue().getTierNo();
                }
            }
        }
        return tierNo;
    }

    private static boolean discByWf(Map<Integer, WorkMove> rowNoMoveMap, WorkingData workingData, StructureData structureData) {
        boolean lock = true;
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            if (CWPDomain.DL_TYPE_DISC.equals(entry.getValue().getDlType())) {
                if (structureData.isLockVMSot(entry.getValue().getOneVMSlot())) { //顶层move有需要拆锁钮的
                    lock = false;
                }
            }
        }
        return false;
    }

    private static boolean discByTier(Map<Integer, WorkMove> rowNoMoveMap, WorkingData workingData, StructureData structureData) {
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            if (CWPDomain.DL_TYPE_DISC.equals(entry.getValue().getDlType())) {
                if (structureData.isLockVMSot(entry.getValue().getOneVMSlot())) { //顶层move有需要拆锁钮的
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasWorkFirst(Integer bayNo, Map<Integer, WorkMove> rowNoMoveMap) {
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            if (entry.getValue().getBayNo().equals(bayNo) && CWPDomain.YES.equals(entry.getValue().getWorkFirst()) && entry.getValue().getWorkFirstOrder() != null) { // 顶层有需要优先作业的move，且已经编序
                return true;
            }
        }
        return false;
    }
}
