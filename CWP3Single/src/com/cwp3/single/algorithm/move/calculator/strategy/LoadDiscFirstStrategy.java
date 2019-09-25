package com.cwp3.single.algorithm.move.calculator.strategy;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.work.WorkMove;
import com.cwp3.single.data.MoveData;

import java.util.*;

/**
 * Created by csw on 2018/9/4.
 * Description:
 */
public class LoadDiscFirstStrategy  extends Strategy{

    public LoadDiscFirstStrategy(Long hatchId, Integer bayNo, Map<Integer, WorkMove> rowNoMoveMap, MoveData moveData, WorkingData workingData, StructureData structureData) {
        super(hatchId, bayNo, rowNoMoveMap, moveData, workingData, structureData);
    }

    @Override
    public String getCurWorkFlow(String curWorkFlow) {
        return null;
    }

    @Override
    public int getCurWorkTierNo(String curWorkFlow, List<WorkMove> delayWorkMoveList) {
        return 0;
    }

    @Override
    public WorkMove getFirstWorkMove(String curWorkFlow, int curTierNo, List<WorkMove> delayWorkMoveList) {
        // 如果有舱盖板，则优先作业舱盖板
        List<WorkMove> workMoveList = new ArrayList<>();
        for (Map.Entry<Integer, WorkMove> entry : rowNoMoveMap.entrySet()) {
            if (CWPDomain.MOVE_TYPE_HC.equals(entry.getValue().getMoveType())) {
                return entry.getValue();
            }
            if (CWPDomain.YES.equals(entry.getValue().getWorkFirst()) && entry.getValue().getWorkFirstOrder() != null) {
                workMoveList.add(entry.getValue());
            }
        }
        if (workMoveList.size() > 0) {
            Collections.sort(workMoveList, new Comparator<WorkMove>() {
                @Override
                public int compare(WorkMove o1, WorkMove o2) {
                    return o1.getWorkFirstOrder().compareTo(o2.getWorkFirstOrder());
                }
            });
            return workMoveList.get(0);
        }
        return null;
    }
}
