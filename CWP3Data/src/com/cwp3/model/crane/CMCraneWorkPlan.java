package com.cwp3.model.crane;

import java.util.Date;

/**
 * Created by csw on 2017/8/14.
 * Description: 航次桥机作业计划信息，主要设置桥吊的装卸效率，作业开工时间等
 */
public class CMCraneWorkPlan {

    private Long berthId;         //靠泊ID
    private String craneNo;//桥吊ID
    private Date workPlanStartTime;//作业计划开始时间
    private Date workPlanEndTime;//作业计划结束时间
    private Integer craneSeq;//桥吊序列号
    private Date workActStartTime;//作业实际开始时间
    private Date workActEndTime;//作业实际结束时间
    private String workStatus;//作业状态
    private Integer cwpLefficiency;//桥吊装船效率
    private Integer cwpDefficiency;//桥吊卸船效率

    public Integer getCraneSeq() {
        return craneSeq;
    }

    public void setCraneSeq(Integer craneSeq) {
        this.craneSeq = craneSeq;
    }

    public CMCraneWorkPlan(Long berthId, String craneNo) {
        this.berthId = berthId;
        this.craneNo = craneNo;
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

    public Date getWorkPlanStartTime() {
        return workPlanStartTime;
    }

    public void setWorkPlanStartTime(Date workPlanStartTime) {
        this.workPlanStartTime = workPlanStartTime;
    }

    public Date getWorkPlanEndTime() {
        return workPlanEndTime;
    }

    public void setWorkPlanEndTime(Date workPlanEndTime) {
        this.workPlanEndTime = workPlanEndTime;
    }

    public Date getWorkActStartTime() {
        return workActStartTime;
    }

    public void setWorkActStartTime(Date workActStartTime) {
        this.workActStartTime = workActStartTime;
    }

    public Date getWorkActEndTime() {
        return workActEndTime;
    }

    public void setWorkActEndTime(Date workActEndTime) {
        this.workActEndTime = workActEndTime;
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public Integer getCwpLefficiency() {
        return cwpLefficiency;
    }

    public void setCwpLefficiency(Integer cwpLefficiency) {
        this.cwpLefficiency = cwpLefficiency;
    }

    public Integer getCwpDefficiency() {
        return cwpDefficiency;
    }

    public void setCwpDefficiency(Integer cwpDefficiency) {
        this.cwpDefficiency = cwpDefficiency;
    }
}
