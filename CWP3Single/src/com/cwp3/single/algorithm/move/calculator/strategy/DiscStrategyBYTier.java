package com.cwp3.single.algorithm.move.calculator.strategy;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.data.MoveData;

import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2018/9/18.
 * Description:
 */
public class DiscStrategyBYTier extends Strategy {

    public DiscStrategyBYTier(Long hatchId, Integer bayNo, Map<Integer, WorkMove> rowNoMoveMap, MoveData moveData, WorkingData workingData, StructureData structureData) {
        super(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
    }

    @Override
    public String getCurWorkFlow(String curWorkFlow) {
        return null;
    }

    @Override
    public int getCurWorkTierNo(String curWorkFlow, List<WorkMove> delayWorkMoveList) {
        return getCurTopTierNo();
    }

    @Override
    public WorkMove getFirstWorkMove(String curWorkFlow, int curTierNo, List<WorkMove> delayWorkMoveList) {
        return getCurTopLandMove(curTierNo);
    }
}
