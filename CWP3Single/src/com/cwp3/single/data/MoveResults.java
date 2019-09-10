package com.cwp3.single.data;

import com.cwp3.model.work.WorkMove;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by csw on 2018/5/31.
 * Description: 根据当前顶层，计算当前可作业量、总量、倍位箱子分布、垫脚/单吊箱信息
 */
public class MoveResults {

    private Map<Integer, Map<Integer, List<WorkMove>>> availableMoveMap; //计算出的可作业Move，按档存 <bayNo,<hcSeq,moveList>>

    private Map<Integer, Map<Integer, List<WorkMove>>> currentTotalMoveMap;

    public MoveResults() {
        availableMoveMap = new ConcurrentHashMap<>();
        currentTotalMoveMap = new HashMap<>();
    }

    private void addMoveToMap(Integer bayNo, Integer hcSeq, WorkMove workMove, Map<Integer, Map<Integer, List<WorkMove>>> moveMap) {
        if (moveMap.get(bayNo) == null) {
            moveMap.put(bayNo, new ConcurrentHashMap<Integer, List<WorkMove>>());
        }
        if (moveMap.get(bayNo).get(hcSeq) == null) {
            moveMap.get(bayNo).put(hcSeq, new ArrayList<WorkMove>());
        }
        moveMap.get(bayNo).get(hcSeq).add(workMove);
    }

    public void addAvailableMove(Integer bayNo, Integer hcSeq, WorkMove workMove) {
        addMoveToMap(bayNo, hcSeq, workMove, availableMoveMap);
    }

    public void clearAvailableMoveByBayNo(Integer bayNo) {
        if (availableMoveMap.get(bayNo) != null) {
            availableMoveMap.get(bayNo).clear();
        }
    }

    public void clearCurrentTotalMoveByBayNo(Integer bayNo) {
        if (currentTotalMoveMap.get(bayNo) != null) {
            currentTotalMoveMap.get(bayNo).clear();
        }
    }

    public void addCurrentTotalMove(Integer bayNo, Integer hcSeq, WorkMove workMove) {
        addMoveToMap(bayNo, hcSeq, workMove, currentTotalMoveMap);
    }

    public Map<Integer, List<WorkMove>> getTolWorkMoveMapByBayNo(Integer bayNo) {
        if (currentTotalMoveMap.get(bayNo) != null) {
            return currentTotalMoveMap.get(bayNo);
        }
        return new HashMap<>();
    }

    public Map<Integer, List<WorkMove>> getAvailableWorkMoveMapByBayNo(Integer bayNo) {
        if (availableMoveMap.get(bayNo) != null) {
            return availableMoveMap.get(bayNo);
        }
        return new HashMap<>();
    }

    public MoveResults deepCopy() {
        MoveResults moveResults = new MoveResults();
        for (Map.Entry<Integer, Map<Integer, List<WorkMove>>> entry : this.availableMoveMap.entrySet()) {
            for (Map.Entry<Integer, List<WorkMove>> entry1 : entry.getValue().entrySet()) {
                for (WorkMove workMove : entry1.getValue()) {
                    moveResults.addAvailableMove(entry.getKey(), entry1.getKey(), workMove);
                }
            }
        }
        for (Map.Entry<Integer, Map<Integer, List<WorkMove>>> entry : this.currentTotalMoveMap.entrySet()) {
            for (Map.Entry<Integer, List<WorkMove>> entry1 : entry.getValue().entrySet()) {
                for (WorkMove workMove : entry1.getValue()) {
                    moveResults.addCurrentTotalMove(entry.getKey(), entry1.getKey(), workMove);
                }
            }
        }
        return moveResults;
    }
}
