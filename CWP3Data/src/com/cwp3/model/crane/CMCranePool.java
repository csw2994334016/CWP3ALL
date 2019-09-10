package com.cwp3.model.crane;


import java.util.Date;

/**
 * Created by csw on 2017/8/14.
 * Description: 桥机池桥机信息，该桥机池含有多少桥机，
 */
public class CMCranePool {

    private Long poolId;         //桥吊池ID
    private String craneNo;//桥吊ID
    private Date workStartTime;//作业开始时间
    private Date workEndTime;//作业结束时间
    private Boolean firstCraneFlag; //开始作业的桥机标志
    private Integer firstWorkBayNo; //开始作业的倍位
    private Long firstWorkAmount; //在这个倍位作业的量，关数

    public CMCranePool(Long poolId, String craneNo) {
        this.poolId = poolId;
        this.craneNo = craneNo;
    }

    public Long getPoolId() {
        return poolId;
    }

    public String getCraneNo() {
        return craneNo;
    }

    public Date getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(Date workStartTime) {
        this.workStartTime = workStartTime;
    }

    public Date getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(Date workEndTime) {
        this.workEndTime = workEndTime;
    }

    public Boolean getFirstCraneFlag() {
        return firstCraneFlag;
    }

    public void setFirstCraneFlag(Boolean firstCraneFlag) {
        this.firstCraneFlag = firstCraneFlag;
    }

    public Integer getFirstWorkBayNo() {
        return firstWorkBayNo;
    }

    public void setFirstWorkBayNo(Integer firstWorkBayNo) {
        this.firstWorkBayNo = firstWorkBayNo;
    }

    public Long getFirstWorkAmount() {
        return firstWorkAmount;
    }

    public void setFirstWorkAmount(Long firstWorkAmount) {
        this.firstWorkAmount = firstWorkAmount;
    }
}
