package com.cwp3.multiple.data;

import com.cwp3.model.vessel.VMContainerAmount;
import com.cwp3.model.vessel.VMSchedule;
import com.cwp3.multiple.model.Schedule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by csw on 2018/11/12.
 * Description:
 */
public class VesselData {

    private Schedule schedule; // 自己的船舶对象
    private List<VMContainerAmount> vmContainerAmountList;// 进出口船图箱信息，计算过程中需要深复制
    private Long allAmount; // 船舶总箱量作业时间
    private Set<String> firstCraneNoSet; // 初步确定桥机

    // 按时间段分析过程中动态改变的数据
    private Long dpCurTime; // 当前时间
    private Long dpRemainAmount; // 当前时间，船舶剩余总箱量作业时间
    private Double dpCurMinCraneNum; // 当前时间，满足船期的最少桥机数目
    private Double dpMomMinCraneNum;// 当前时间，满足船期的最少桥机数目

    public VesselData(Schedule schedule) {
        this.schedule = schedule;
        firstCraneNoSet = new HashSet<>();
        vmContainerAmountList = new ArrayList<>();
        dpRemainAmount = 0L;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public VMSchedule getVmSchedule() {
        return schedule.getVmSchedule();
    }

    public Long getBerthId() {
        return schedule.getVmSchedule().getBerthId();
    }

    public String getVesselCode() {
        return schedule.getVmSchedule().getVesselCode();
    }

    public List<VMContainerAmount> getVmContainerAmountList() {
        return vmContainerAmountList;
    }

    public Set<String> getFirstCraneNoSet() {
        return firstCraneNoSet;
    }

    public Long getDpCurTime() {
        return dpCurTime;
    }

    public void setDpCurTime(Long dpCurTime) {
        this.dpCurTime = dpCurTime;
    }

    public Double getDpCurMinCraneNum() {
        return dpCurMinCraneNum;
    }

    public int getDpCurCeilMinCraneNum() {
        return (int) Math.ceil(dpCurMinCraneNum);
    }

    public int getDpCurFloorMinCraneNum() {
        return (int) Math.floor(dpCurMinCraneNum);
    }

    public void setDpCurMinCraneNum(Double dpCurMinCraneNum) {
        this.dpCurMinCraneNum = dpCurMinCraneNum;
    }

    public Long getDpRemainAmount() {
        return dpRemainAmount;
    }

    public void setDpRemainAmount(Long dpRemainAmount) {
        this.dpRemainAmount = dpRemainAmount;
    }

    public Long getAllAmount() {
        return allAmount;
    }

    public void setAllAmount(Long allAmount) {
        this.allAmount = allAmount;
    }

    public Double getDpMomMinCraneNum() {
        return dpMomMinCraneNum;
    }

    public void setDpMomMinCraneNum(Double dpMomMinCraneNum) {
        this.dpMomMinCraneNum = dpMomMinCraneNum;
    }
}
