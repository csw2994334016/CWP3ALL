package com.cwp3.model.work;

import java.util.Date;

/**
 * Created by csw on 2018/11/29.
 * Description:
 */
public class CranePlan {

    private Long berthId;//靠泊ID
    private String craneNo;//桥机ID
    private Date workingStartTime;  //开始作业时间
    private Date workingEndTime;   //结束作业时间

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
}
