package com.cwp3.single.algorithm.move.calculator.strategy;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.algorithm.move.method.PublicMethod;
import com.cwp3.single.data.MoveData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2018/9/4.
 * Description:
 */
public abstract class Strategy {

    Long hatchId;
    Integer bayNo;
    Map<Integer, WorkMove> rowNoMoveMap;
    MoveData moveData;
    WorkingData workingData;
    StructureData structureData;

    Strategy(Long hatchId, Integer bayNo, Map<Integer, WorkMove> rowNoMoveMap, MoveData moveData, WorkingData workingData, StructureData structureData) {
        this.hatchId = hatchId;
        this.bayNo = bayNo;
        this.rowNoMoveMap = rowNoMoveMap;
        this.moveData = moveData;
        this.workingData = workingData;
        this.structureData = structureData;
    }

    public abstract String getCurWorkFlow(String curWorkFlow);

    public abstract int getCurWorkTierNo(String curWorkFlow, List<WorkMove> delayWorkMoveList);

    public abstract WorkMove getFirstWorkMove(String curWorkFlow, int curTierNo, List<WorkMove> delayWorkMoveList);

    String getCurWorkFlowByDefault(String dlType, String curWorkFlow, Integer bayNo, Map<Integer, WorkMove> rowNoMoveMap, MoveData moveData) {
        if (curWorkFlow == null) { //一般是第一次编序，从moveData中查找该倍位作业应该继续什么工艺作业
            curWorkFlow = moveData.getCurWorkFlowByBayNo(bayNo); //上次DP决策结束是什么工艺
        }
        if (curWorkFlow != null) { //判断顶层是否有满足当前作业工艺的workMove
            for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
                if (dlType.equals(entry.getValue().getDlType()) && entry.getValue().getWorkFlow().equals(curWorkFlow)) {
                    return curWorkFlow;
                }
            }
        }
        // 如果甲板上或者甲板下都有顶层时,则需要判断:装船优先做完甲板下;卸船优先做完甲板上
        Map<Integer, WorkMove> rowNoMoveMapNew = new HashMap<>();
        if (CWPDomain.DL_TYPE_LOAD.equals(dlType)) {
            for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
                if (dlType.equals(entry.getValue().getDlType()) && entry.getValue().getTierNo() < 50) {
                    rowNoMoveMapNew.put(entry.getKey(), entry.getValue());
                }
            }
        } else {
            for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
                if (dlType.equals(entry.getValue().getDlType()) && entry.getValue().getTierNo() > 50) {
                    rowNoMoveMapNew.put(entry.getKey(), entry.getValue());
                }
            }
        }
        if (rowNoMoveMapNew.size() == 0) {
            rowNoMoveMapNew = rowNoMoveMap;
        }
        // 证明需要改变作业工艺，一般是大倍位置上，CT_SINGLE20-->CT_DUAL20-->CT_SINGLE40-->CT_DUAL40-->CT_QUAD20-->CT_HATCH_COVER，默认选中优先级最低的作业工艺，一般也是在大倍位置上才会有
        // 如果是双吊具，则继续作业双吊具，CT_DUAL40-->CT_QUAD20互相切换
        List<String> doubleWorkFlowList = Arrays.asList(CWPCraneDomain.CT_DUAL40, CWPCraneDomain.CT_QUAD20);
        if (curWorkFlow != null && doubleWorkFlowList.contains(curWorkFlow)) { //判断顶层是否有满足当前作业工艺的workMove
            for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
                if (dlType.equals(entry.getValue().getDlType()) && doubleWorkFlowList.contains(entry.getValue().getWorkFlow())) {
                    return entry.getValue().getWorkFlow();
                }
            }
        }
        List<String> workFlowList = PublicMethod.getAllWorkFlowSeqList();
        curWorkFlow = CWPCraneDomain.CT_HATCH_COVER;
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMapNew.entrySet()) {
            if (dlType.equals(entry.getValue().getDlType())) {
                String workFlow = entry.getValue().getWorkFlow();
                if (workFlowList.indexOf(workFlow) < workFlowList.indexOf(curWorkFlow)) {
                    curWorkFlow = workFlow;
                }
            }
        }
        return curWorkFlow;
    }

    int getCurTopTierNo() {
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

    WorkMove getCurTopLandMove(int curTierNo) {
        String oddOrEven = workingData.getOddOrEvenBySeaOrLand(CWPDomain.ROW_SEQ_LAND_SEA);
        List<Integer> rowNoSeqList = structureData.getRowSeqListBySeaOrLand(hatchId, oddOrEven);
        WorkMove workMove = null;
        for (Integer rowNo : rowNoSeqList) {
            WorkMove workMove1 = rowNoMoveMap.get(rowNo);
            if (workMove1 != null && workMove1.getTierNo() == curTierNo && workMove1.getDlType().equals(CWPDomain.DL_TYPE_DISC)) {
                workMove = workMove1;
                break;
            }
        }
        return workMove;
    }

    int getCurBottomTierNo() {
        int tierNo = 1000; //可作业顶层最大层号
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            if (CWPDomain.DL_TYPE_LOAD.equals(entry.getValue().getDlType())) {
                if (entry.getValue().getTierNo() < tierNo) {
                    tierNo = entry.getValue().getTierNo();
                }
            }
        }
        return tierNo;
    }

    WorkMove getCurBottomSeaMove(int curTierNo) {
        String oddOrEven = workingData.getOddOrEvenBySeaOrLand(CWPDomain.ROW_SEQ_SEA_LAND);
        List<Integer> rowNoSeqList = structureData.getRowSeqListBySeaOrLand(hatchId, oddOrEven);
        WorkMove workMove = null;
        for (Integer rowNo : rowNoSeqList) {
            WorkMove workMove1 = rowNoMoveMap.get(rowNo);
            if (workMove1 != null && workMove1.getTierNo() == curTierNo && workMove1.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) {
                workMove = workMove1;
                break;
            }
        }
        return workMove;
    }
}
