package com.cwp3.single.data;

import com.cwp3.domain.CWPDomain;
import com.cwp3.model.vessel.VMSlot;
import com.cwp3.model.work.WorkMove;

import java.util.*;

/**
 * Created by csw on 2018/5/31.
 * Description: 每次决策计算剩余量的依据
 */
public class MoveData {

    private Map<String, WorkMove> discWorkMoveMap;       //卸船Move信息,key: vLocation 小位置，
    private Map<String, WorkMove> loadWorkMoveMap;       //装船Move信息,key

    private Map<Long, Set<WorkMove>> curTopMoveMap; //当前时刻，舱内的可作业的顶层move：<hatchId, Set<WorkMove>>
    private Map<Long, Long> curMoveOrderMap; //当前时刻，舱内的关号
    private Map<Long, String> curWorkFlowMap; //当前时刻，舱内的作业工艺
    private Map<Integer, String> curWorkFlowMap1; // 倍位上次DP决策结束是什么工艺

    public MoveData() {
        discWorkMoveMap = new HashMap<>();
        loadWorkMoveMap = new HashMap<>();
        curTopMoveMap = new HashMap<>();
        curMoveOrderMap = new HashMap<>();
        curWorkFlowMap = new HashMap<>();
        curWorkFlowMap1 = new HashMap<>();
    }

    public Map<String, WorkMove> getDiscWorkMoveMap() {
        return discWorkMoveMap;
    }

    public void setDiscWorkMoveMap(Map<String, WorkMove> discWorkMoveMap) {
        this.discWorkMoveMap = discWorkMoveMap;
    }

    public Map<String, WorkMove> getLoadWorkMoveMap() {
        return loadWorkMoveMap;
    }

    public void setLoadWorkMoveMap(Map<String, WorkMove> loadWorkMoveMap) {
        this.loadWorkMoveMap = loadWorkMoveMap;
    }

    public WorkMove getWorkMoveByVMSlot(VMSlot vmSlot, String dlType) {
        if (vmSlot != null && dlType.equals(CWPDomain.DL_TYPE_DISC)) {
            return discWorkMoveMap.get(vmSlot.getVmPosition().getVLocation());
        }
        if (vmSlot != null && dlType.equals(CWPDomain.DL_TYPE_LOAD)) {
            return loadWorkMoveMap.get(vmSlot.getVmPosition().getVLocation());
        }
        return null;
    }

    public void putCurTopWorkMove(Long hatchId, WorkMove workMove) {
        if (curTopMoveMap.get(hatchId) == null) {
            curTopMoveMap.put(hatchId, new LinkedHashSet<WorkMove>());
        }
        curTopMoveMap.get(hatchId).add(workMove);
    }

    private Set<WorkMove> getCurTopMoveSetByHatchId(Long hatchId) {
        if (curTopMoveMap.get(hatchId) != null) {
            return curTopMoveMap.get(hatchId);
        }
        return new HashSet<>();
    }

    public List<WorkMove> getCurTopMoveListByHatchIdAndBayNo(Long hatchId, Integer bayNo) {
        List<WorkMove> workMoveList = new ArrayList<>();
        for (WorkMove workMove : this.getCurTopMoveSetByHatchId(hatchId)) {
            if (workMove.getBayNo().equals(bayNo)) {
                workMoveList.add(workMove);
            }
        }
        return workMoveList;
    }

    public void clearCurTopMoveByHatchId(Long hatchId) {
        if (curTopMoveMap.get(hatchId) != null) {
            curTopMoveMap.get(hatchId).clear();
        }
    }

    public Map<Long, Long> getCurMoveOrderMap() {
        return curMoveOrderMap;
    }

    public long getCurMoveOrder(Long hatchId) {
        if (curMoveOrderMap.get(hatchId) == null) {
            curMoveOrderMap.put(hatchId, 1L);
        }
        return curMoveOrderMap.get(hatchId);
    }

    public void setCurMoveOrder(Long hatchId, Long moveOrder) {
        curMoveOrderMap.put(hatchId, moveOrder);
    }

    public Map<Long , String> getCurWorkFlowMap() {
        return curWorkFlowMap;
    }

    public String getCurWorkFlowByHatchId(Long hatchId) {
        return curWorkFlowMap.get(hatchId);
    }

    public void setCurWorkFlow(Long hatchId, String workFlow) {
        curWorkFlowMap.put(hatchId, workFlow);
    }

    public Map<Integer, String> getCurWorkFlowMap1() {
        return curWorkFlowMap1;
    }

    public void setCurWorkFlow1(Integer bayNo, String workFlow) {
        curWorkFlowMap1.put(bayNo, workFlow);
    }

    public String getCurWorkFlowByBayNo(Integer bayNo) {
        return curWorkFlowMap1.get(bayNo);
    }

    public List<WorkMove> getTotalMoveListByBayNo(Integer bayNo) {
        List<WorkMove> workMoveList = new ArrayList<>();
        for (WorkMove workMove : discWorkMoveMap.values()) {
            if (workMove.getMoveOrder() == null && workMove.getBayNo().equals(bayNo)) {
                workMoveList.add(workMove);
            }
        }
        for (WorkMove workMove : loadWorkMoveMap.values()) {
            if (workMove.getMoveOrder() == null && workMove.getBayNo().equals(bayNo)) {
                workMoveList.add(workMove);
            }
        }
        return workMoveList;
    }

}
