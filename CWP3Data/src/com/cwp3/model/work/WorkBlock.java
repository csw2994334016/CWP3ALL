package com.cwp3.model.work;

import java.util.Date;

/**
 * Created by csw on 2018/7/3.
 * Description:
 */
public class WorkBlock {

    private Long berthId; //靠泊ID
    private String craneNo; //桥机ID
    private String bayNo; //倍位No
    private String currentCraneBayNo;//桥机当前所在倍位
    private Long hatchId; //舱ID
    private Long planAmount; //作业块Move总数，计划作业总量
    private Double cranePosition;//桥机当前位置
    private String lduldfg; //装卸船标志
    private Long craneSeq;//作业某个舱所有桥机的作业顺序
    private Long hatchSeq;//某个桥机作业哪些舱的顺序
    private Date workingStartTime; //作业块计划开工时间
    private Date workingEndTime; //作业块计划完工时间
    private Long blockId; //作业块Id
    private String deleteFlag; //删除标记，当传入作业块相应倍位没有待作业的指令时，作业块标记为"Y"，否则为null或者为"N"
    private String selectReason; //桥机选择倍位作业的原因
    private String blockType; //作业块类型，"HC"表示舱盖板作业块；"CNT"表示集装箱作业块

    public String getBlockType() {
        return blockType;
    }

    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public String getSelectReason() {
        return selectReason;
    }

    public void setSelectReason(String selectReason) {
        this.selectReason = selectReason;
    }

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public String getCraneNo() {
        return craneNo;
    }

    public void setCraneNo(String craneNo) {
        this.craneNo = craneNo;
    }

    public String getBayNo() {
        return bayNo;
    }

    public void setBayNo(String bayNo) {
        this.bayNo = bayNo;
    }

    public String getCurrentCraneBayNo() {
        return currentCraneBayNo;
    }

    public void setCurrentCraneBayNo(String currentCraneBayNo) {
        this.currentCraneBayNo = currentCraneBayNo;
    }

    public Long getHatchId() {
        return hatchId;
    }

    public void setHatchId(Long hatchId) {
        this.hatchId = hatchId;
    }

    public Long getPlanAmount() {
        return planAmount;
    }

    public void setPlanAmount(Long planAmount) {
        this.planAmount = planAmount;
    }

    public Double getCranePosition() {
        return cranePosition;
    }

    public void setCranePosition(Double cranePosition) {
        this.cranePosition = cranePosition;
    }

    public String getLduldfg() {
        return lduldfg;
    }

    public void setLduldfg(String lduldfg) {
        this.lduldfg = lduldfg;
    }

    public Long getCraneSeq() {
        return craneSeq;
    }

    public void setCraneSeq(Long craneSeq) {
        this.craneSeq = craneSeq;
    }

    public Long getHatchSeq() {
        return hatchSeq;
    }

    public void setHatchSeq(Long hatchSeq) {
        this.hatchSeq = hatchSeq;
    }

    public Date getWorkingStartTime() {
        return workingStartTime;
    }

    public void setWorkingStartTime(Date workingStartTime) {
        this.workingStartTime = workingStartTime;
    }

    public Date getWorkingEndTime() {
        return workingEndTime;
    }

    public void setWorkingEndTime(Date workingEndTime) {
        this.workingEndTime = workingEndTime;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public String getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(String deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

}
