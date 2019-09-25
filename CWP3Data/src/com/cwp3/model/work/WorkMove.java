package com.cwp3.model.work;

import com.cwp3.model.vessel.VMSlot;

import java.io.Serializable;
import java.util.*;

/**
 * Created by csw on 2018/4/2.
 * Description: 桥机作业关的定义，即桥机作业一次的动作叫做一关，其中包含舱盖板、集装箱，装卸类型、作业工艺、作业耗时是必要字段
 */
public class WorkMove implements Serializable {

    private String dlType; //装卸类型
    private String workFlow; //作业工艺
    private Long workTime; //这一关作业对象的作业时间，单位：秒
    private String moveType; //舱盖板/集装箱
    private Long hatchId;
    private Set<VMSlot> vmSlotSet;  //Move占用的slots
    private List<Integer> hcRowNoList; //舱盖板move专用
    private List<VMSlot> hcVMSlotList; // 舱盖板move专用，用于判断舱盖板move是否是顶层
    private List<VMSlot> hcVMSlotListD; // 舱盖板move专用，用于判断装船舱盖板move是否是顶层，slot对应的卸船集装箱move
    private Integer bayNo;
    private Integer rowNo;
    private Integer tierNo;
    private Integer hcSeq;
    private String workFirst; // 优先作业的move
    private Long workFirstOrder; // 优先作业顺序
    private String hcWorkFlow; // 舱盖板move专用，表示舱盖板使用什么吊具作业：S4/D4
    private String cwoCraneNo; // 人工锁定作业块，move指定给该桥机作业

    private Long moveOrder; //作业顺序
    private Date planStartTime; //计划开始时间
    private Date planEndTime; //计划结束时间
    private String selectReason;
    private String craneNo;
    private Double workPosition;
    private Long blockId;

    public WorkMove() {
    }

    public WorkMove(String dlType, String moveType) {
        this.dlType = dlType;
        this.moveType = moveType;
        this.vmSlotSet = new HashSet<>();
        this.hcRowNoList = new ArrayList<>();
        this.hcVMSlotList = new ArrayList<>();
        this.hcVMSlotListD = new ArrayList<>();
    }

    public WorkMove(String dlType, String workFlow, Long workTime, String moveType) {
        this.dlType = dlType;
        this.workFlow = workFlow;
        this.workTime = workTime;
        this.moveType = moveType;
        this.vmSlotSet = new HashSet<>();
        this.hcRowNoList = new ArrayList<>();
        this.hcVMSlotList = new ArrayList<>();
        this.hcVMSlotListD = new ArrayList<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WorkMove) {
            WorkMove workMove = (WorkMove) obj;
            return dlType.equals(workMove.getDlType()) && sameVMSlot(workMove.getVmSlotSet());
        } else {
            return false;
        }
    }

    private boolean sameVMSlot(Set<VMSlot> vmSlotSet) {
        if (this.vmSlotSet.size() != vmSlotSet.size()) {
            return false;
        } else {
            Set<VMSlot> vmSlotSetTemp = new HashSet<>();
            vmSlotSetTemp.addAll(vmSlotSet);
            vmSlotSetTemp.addAll(this.vmSlotSet);
            return vmSlotSetTemp.size() == this.vmSlotSet.size();
        }
    }

    public VMSlot getOneVMSlot() {
        List<VMSlot> vmSlotList = new ArrayList<>(vmSlotSet);
        return vmSlotList.get(0);
    }

    public WorkMove baseCopy() {
        WorkMove workMoveCopy = new WorkMove(dlType, workFlow, workTime, moveType);
        workMoveCopy.setHatchId(hatchId);
        workMoveCopy.setVmSlotSet(vmSlotSet);
        workMoveCopy.setHcRowNoList(hcRowNoList);
        workMoveCopy.setHcVMSlotList(hcVMSlotList);
        workMoveCopy.setHcVMSlotListD(hcVMSlotListD);
        workMoveCopy.setBayNo(bayNo);
        workMoveCopy.setRowNo(rowNo);
        workMoveCopy.setTierNo(tierNo);
        workMoveCopy.setHcSeq(hcSeq);
        workMoveCopy.setWorkFirst(workFirst);
        workMoveCopy.setWorkFirstOrder(workFirstOrder);
        workMoveCopy.setHcWorkFlow(hcWorkFlow);
        workMoveCopy.setCwoCraneNo(cwoCraneNo);
        return workMoveCopy;
    }

    public int getRowNoNumber() {
        Set<Integer> rowNoSet = new HashSet<>();
        for (VMSlot vmSlot : vmSlotSet) {
            rowNoSet.add(vmSlot.getVmPosition().getRowNo());
        }
        return rowNoSet.size();
    }

    public void setVmSlotSet(Set<VMSlot> vmSlotSet) {
        this.vmSlotSet = vmSlotSet;
    }

    public void setDlType(String dlType) {
        this.dlType = dlType;
    }

    public String getDlType() {
        return dlType;
    }

    public String getWorkFlow() {
        return workFlow;
    }

    public void setWorkFlow(String workFlow) {
        this.workFlow = workFlow;
    }

    public Long getWorkTime() {
        return workTime;
    }

    public void setWorkTime(Long workTime) {
        this.workTime = workTime;
    }

    public String getMoveType() {
        return moveType;
    }

    public void setMoveType(String moveType) {
        this.moveType = moveType;
    }

    public Long getMoveOrder() {
        return moveOrder;
    }

    public void setMoveOrder(Long moveOrder) {
        this.moveOrder = moveOrder;
    }

    public Set<VMSlot> getVmSlotSet() {
        return vmSlotSet;
    }

    public void addVmSlot(VMSlot vmSlot) {
        this.vmSlotSet.add(vmSlot);
    }

    public List<Integer> getHcRowNoList() {
        return hcRowNoList;
    }

    public void setHcRowNoList(List<Integer> hcRowNoList) {
        this.hcRowNoList = hcRowNoList;
    }

    public Integer getBayNo() {
        return bayNo;
    }

    public void setBayNo(Integer bayNo) {
        this.bayNo = bayNo;
    }

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }

    public Integer getTierNo() {
        return tierNo;
    }

    public void setTierNo(Integer tierNo) {
        this.tierNo = tierNo;
    }

    public Integer getHcSeq() {
        return hcSeq;
    }

    public void setHcSeq(Integer hcSeq) {
        this.hcSeq = hcSeq;
    }

    public Date getPlanStartTime() {
        return planStartTime;
    }

    public void setPlanStartTime(Date planStartTime) {
        this.planStartTime = planStartTime;
    }

    public Date getPlanEndTime() {
        return planEndTime;
    }

    public void setPlanEndTime(Date planEndTime) {
        this.planEndTime = planEndTime;
    }

    public String getCraneNo() {
        return craneNo;
    }

    public void setCraneNo(String craneNo) {
        this.craneNo = craneNo;
    }

    public Double getWorkPosition() {
        return workPosition;
    }

    public void setWorkPosition(Double workPosition) {
        this.workPosition = workPosition;
    }

    public Long getHatchId() {
        return hatchId;
    }

    public void setHatchId(Long hatchId) {
        this.hatchId = hatchId;
    }

    public String getSelectReason() {
        return selectReason;
    }

    public void setSelectReason(String selectReason) {
        this.selectReason = selectReason;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public String getWorkFirst() {
        return workFirst;
    }

    public void setWorkFirst(String workFirst) {
        this.workFirst = workFirst;
    }

    public Long getWorkFirstOrder() {
        return workFirstOrder;
    }

    public void setWorkFirstOrder(Long workFirstOrder) {
        this.workFirstOrder = workFirstOrder;
    }

    public List<VMSlot> getHcVMSlotList() {
        return hcVMSlotList;
    }

    public void setHcVMSlotList(List<VMSlot> hcVMSlotList) {
        this.hcVMSlotList = hcVMSlotList;
    }

    public List<VMSlot> getHcVMSlotListD() {
        return hcVMSlotListD;
    }

    public void setHcVMSlotListD(List<VMSlot> hcVMSlotListD) {
        this.hcVMSlotListD = hcVMSlotListD;
    }

    public String getHcWorkFlow() {
        return hcWorkFlow;
    }

    public void setHcWorkFlow(String hcWorkFlow) {
        this.hcWorkFlow = hcWorkFlow;
    }

    public String getCwoCraneNo() {
        return cwoCraneNo;
    }

    public void setCwoCraneNo(String cwoCraneNo) {
        this.cwoCraneNo = cwoCraneNo;
    }
}
