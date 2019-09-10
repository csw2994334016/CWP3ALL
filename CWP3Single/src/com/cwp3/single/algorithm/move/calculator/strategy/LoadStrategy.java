package com.cwp3.single.algorithm.move.calculator.strategy;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.data.MoveData;

import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2018/9/4.
 * Description:
 */
public class LoadStrategy extends Strategy {

    public LoadStrategy(Long hatchId, Integer bayNo, Map<Integer, WorkMove> rowNoMoveMap, MoveData moveData, WorkingData workingData, StructureData structureData) {
        super(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
    }

    @Override
    public String getCurWorkFlow(String curWorkFlow) {
        //使用父类的方法
        curWorkFlow = super.getCurWorkFlowByDefault(CWPDomain.DL_TYPE_LOAD, curWorkFlow, bayNo, rowNoMoveMap, moveData);
        //todo:根据船图箱量分布情况，确定先作业哪种工艺比较合适

        return curWorkFlow;
    }

    @Override
    public int getCurWorkTierNo(String curWorkFlow, List<WorkMove> delayWorkMoveList) {
        int tierNo = 1000;
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            if (CWPDomain.DL_TYPE_LOAD.equals(entry.getValue().getDlType())) {
                if (entry.getValue().getWorkFlow().equals(curWorkFlow)) {
                    if (entry.getValue().getTierNo() < tierNo) {
                        tierNo = entry.getValue().getTierNo();
                    }
                }
            }
        }
        return tierNo;
    }

    @Override
    public WorkMove getFirstWorkMove(String curWorkFlow, int curTierNo, List<WorkMove> delayWorkMoveList) {
        String oddOrEven = workingData.getOddOrEvenBySeaOrLand(CWPDomain.ROW_SEQ_SEA_LAND);
        List<Integer> rowNoSeqList = structureData.getRowSeqListBySeaOrLand(hatchId, oddOrEven);
        WorkMove workMove = null;
        for (Integer rowNo : rowNoSeqList) {
            WorkMove workMove1 = rowNoMoveMap.get(rowNo);
            if (workMove1 != null && workMove1.getWorkFlow().equals(curWorkFlow) && workMove1.getTierNo() == curTierNo && workMove1.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) {
                workMove = workMove1;
                break;
            }
        }
        return workMove;
    }
}
