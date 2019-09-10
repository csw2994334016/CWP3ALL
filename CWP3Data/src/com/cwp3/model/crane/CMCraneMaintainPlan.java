package com.cwp3.model.crane;

import java.util.Date;

/**
 * Created by csw on 2017/8/14.
 * Description: 桥机维修计划信息
 */
public class CMCraneMaintainPlan {

    private String craneNo;//桥吊ID
    private Date maintainStartTime	;	//维护开始时间
    private Date maintainEndTime;    //维护结束时间
    private String craneStatus;//桥吊作业状态故障
    private String craneMoveStatus; //是否可以移动

    public CMCraneMaintainPlan(String craneNo) {
        this.craneNo = craneNo;
    }

    public String getCraneNo() {
        return craneNo;
    }

    public Date getMaintainStartTime() {
        return maintainStartTime;
    }

    public void setMaintainStartTime(Date maintainStartTime) {
        this.maintainStartTime = maintainStartTime;
    }

    public Date getMaintainEndTime() {
        return maintainEndTime;
    }

    public void setMaintainEndTime(Date maintainEndTime) {
        this.maintainEndTime = maintainEndTime;
    }

    public String getCraneStatus() {
        return craneStatus;
    }

    public void setCraneStatus(String craneStatus) {
        this.craneStatus = craneStatus;
    }

    public String getCraneMoveStatus() {
        return craneMoveStatus;
    }

    public void setCraneMoveStatus(String craneMoveStatus) {
        this.craneMoveStatus = craneMoveStatus;
    }
}
