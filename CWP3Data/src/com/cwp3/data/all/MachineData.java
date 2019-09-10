package com.cwp3.data.all;

import com.cwp3.model.crane.*;

import java.util.*;

public class MachineData {

    private Map<String, CMCraneType> cmCraneTypeMap;        //桥机类型,key: qcTypeId
    private Map<String, CMCrane> cmCraneMap;      //桥机对象,key: craneNo
    private List<CMCraneMoveRange> cmCraneMoveRangeList; // 桥机物理移动范围，每部桥机作业每条船舶的倍位范围，同一部桥机可能会在不同的船里面出现
    private Map<String, List<CMCraneMaintainPlan>> cmCraneMaintainPlanMap; //桥机维修计划
    private Map<String, List<CMCraneWorkPlan>> cmCraneWorkPlanMap; //桥机作业计划，桥机已经安排给某些船舶的作业计划

    public MachineData() {
        cmCraneTypeMap = new HashMap<>();
        cmCraneMap = new HashMap<>();
        cmCraneMoveRangeList = new ArrayList<>();
        cmCraneMaintainPlanMap = new HashMap<>();
        cmCraneWorkPlanMap = new HashMap<>();
    }

    public Set<String> getAllCraneNo() {
        return cmCraneMap.keySet();
    }

    public void addCMCrane(CMCrane cmCrane) {
        cmCraneMap.put(cmCrane.getCraneNo(), cmCrane);
    }

    public CMCrane getCMCraneByCraneNo(String craneNo) {
        return cmCraneMap.get(craneNo);
    }

    public void addCMCraneType(CMCraneType cmCraneType) {
        cmCraneTypeMap.put(cmCraneType.getCraneTypeId(), cmCraneType);
    }

    public List<CMCraneType> getCMCraneTypes() {
        return new ArrayList<>(cmCraneTypeMap.values());
    }

    public CMCraneType getCMCraneTypeById(String cmCraneTypeId) {
        return cmCraneTypeMap.get(cmCraneTypeId);
    }

    public List<CMCraneMoveRange> getCmCraneMoveRangeList() {
        return cmCraneMoveRangeList;
    }

    public void setCmCraneMoveRangeList(List<CMCraneMoveRange> cmCraneMoveRangeList) {
        this.cmCraneMoveRangeList = cmCraneMoveRangeList;
    }

    public void addCMCraneMaintainPlan(CMCraneMaintainPlan cmCraneMaintainPlan) {
        if (cmCraneMaintainPlanMap.get(cmCraneMaintainPlan.getCraneNo()) == null) {
            cmCraneMaintainPlanMap.put(cmCraneMaintainPlan.getCraneNo(), new ArrayList<CMCraneMaintainPlan>());
        }
        cmCraneMaintainPlanMap.get(cmCraneMaintainPlan.getCraneNo()).add(cmCraneMaintainPlan);
    }

    public List<CMCraneMaintainPlan> getCMCraneMaintainPlanListByCraneNo(String craneNo) {
        return cmCraneMaintainPlanMap.get(craneNo);
    }

    public void addCMCraneWorkPlan(CMCraneWorkPlan cmCraneWorkPlan) {
        if (cmCraneWorkPlanMap.get(cmCraneWorkPlan.getCraneNo()) == null) {
            cmCraneWorkPlanMap.put(cmCraneWorkPlan.getCraneNo(), new ArrayList<CMCraneWorkPlan>());
        }
        cmCraneWorkPlanMap.get(cmCraneWorkPlan.getCraneNo()).add(cmCraneWorkPlan);
    }

    public List<CMCraneWorkPlan> getCMCraneWorkPlanListByCraneNo(String craneNo) {
        return cmCraneWorkPlanMap.get(craneNo);
    }

    public List<CMCrane> getAllCMCraneList() {
        return new ArrayList<>(cmCraneMap.values());
    }

    public CMCraneMoveRange getCmCraneMoveRangeByBerthIdAndCraneNo(Long berthId, String craneNo) {
        for (CMCraneMoveRange cmCraneMoveRange : cmCraneMoveRangeList) {
            if (berthId.equals(cmCraneMoveRange.getBerthId()) && craneNo.equals(cmCraneMoveRange.getCraneNo())) {
                return cmCraneMoveRange;
            }
        }
        return null;
    }
}
