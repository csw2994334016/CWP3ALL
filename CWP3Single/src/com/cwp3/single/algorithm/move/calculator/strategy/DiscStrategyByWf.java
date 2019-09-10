package com.cwp3.single.algorithm.move.calculator.strategy;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.data.MoveData;

import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2018/9/18.
 * Description:
 */
public class DiscStrategyByWf extends Strategy {

    public DiscStrategyByWf(Long hatchId, Integer bayNo, Map<Integer, WorkMove> rowNoMoveMap, MoveData moveData, WorkingData workingData, StructureData structureData) {
        super(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
    }

    @Override
    public String getCurWorkFlow(String curWorkFlow) {
        if (curWorkFlow == null) { //顶层靠陆侧的move作业工艺是什么
            WorkMove workMove = getCurTopLandMove(getCurTopTierNo());
            if (workMove != null) {
                return workMove.getWorkFlow();
            }
        } else { //当前作业工艺是否有满足条件的move，否则切换顶层靠陆侧的move作业工艺

        }
        return null;
    }

    @Override
    public int getCurWorkTierNo(String curWorkFlow, List<WorkMove> delayWorkMoveList) {
        return 0;
    }

    @Override
    public WorkMove getFirstWorkMove(String curWorkFlow, int curTierNo, List<WorkMove> delayWorkMoveList) {
        return null;
    }
}
