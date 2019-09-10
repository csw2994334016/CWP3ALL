package com.cwp3.model.crane;


/**
 * Created by csw on 2017/4/20 11:39.
 * Explain: 桥机基础信息
 */
public class CMCrane {

    private String craneNo;//桥吊ID
    private Double currentCranePosition;//当前位置
    private Integer unLoadEfficiency20;//卸船20英尺单箱箱机械极限效率（一小时多少关）
    private Integer unLoadEfficiency40;//卸船40英尺单箱箱机械极限效率
    private Integer unLoadEfficiencyTwin;// 卸船双箱吊机械极限效率
    private Integer unLoadEfficiencyTdm;// 卸船双吊具机械极限效率
    private Integer loadEfficiency20;//装船20英尺单箱机械极限效率
    private Integer loadEfficiency40;//装船40英尺单箱机械极限效率
    private Integer loadEfficiencyTwin;// 装船双箱吊机械极限效率
    private Integer loadEfficiencyTdm; //装船双吊具机械极限效率
    private Double moveRangeFrom;//移动范围起始点
    private Double moveRangeTo;//移动范围终止点
    private Double craneSafeSpan;//安全距离
    private Integer craneSeq;//桥吊序列号
    private Double craneSpeed;//移动速度，米/分钟
    private Integer craneWidth;//桥吊宽度
    private String craneStatus;//桥吊作业状态故障
    private String craneMoveStatus; //是否可以移动
    private Integer craneMaxCarryWeight;//桥吊的最大作业吨位
    private Long workBerthId;         //靠泊ID
    private String workVesselBay;//当前作业倍位
    private String tandemFlag; // 双吊具工艺标志：Y表示桥机可以做双吊具工艺；N或者null表示桥机不可以做双吊具工艺

    private String craneTypeId;//桥机类型

    public CMCrane(String craneNo) {
        this.craneNo = craneNo;
    }

    public String getCraneNo() {
        return craneNo;
    }

    public Double getCurrentCranePosition() {
        return currentCranePosition;
    }

    public void setCurrentCranePosition(Double currentCranePosition) {
        this.currentCranePosition = currentCranePosition;
    }

    public Integer getUnLoadEfficiency20() {
        return unLoadEfficiency20;
    }

    public void setUnLoadEfficiency20(Integer unLoadEfficiency20) {
        this.unLoadEfficiency20 = unLoadEfficiency20;
    }

    public Integer getUnLoadEfficiency40() {
        return unLoadEfficiency40;
    }

    public void setUnLoadEfficiency40(Integer unLoadEfficiency40) {
        this.unLoadEfficiency40 = unLoadEfficiency40;
    }

    public Integer getUnLoadEfficiencyTwin() {
        return unLoadEfficiencyTwin;
    }

    public void setUnLoadEfficiencyTwin(Integer unLoadEfficiencyTwin) {
        this.unLoadEfficiencyTwin = unLoadEfficiencyTwin;
    }

    public Integer getUnLoadEfficiencyTdm() {
        return unLoadEfficiencyTdm;
    }

    public void setUnLoadEfficiencyTdm(Integer unLoadEfficiencyTdm) {
        this.unLoadEfficiencyTdm = unLoadEfficiencyTdm;
    }

    public Integer getLoadEfficiency20() {
        return loadEfficiency20;
    }

    public void setLoadEfficiency20(Integer loadEfficiency20) {
        this.loadEfficiency20 = loadEfficiency20;
    }

    public Integer getLoadEfficiency40() {
        return loadEfficiency40;
    }

    public void setLoadEfficiency40(Integer loadEfficiency40) {
        this.loadEfficiency40 = loadEfficiency40;
    }

    public Integer getLoadEfficiencyTwin() {
        return loadEfficiencyTwin;
    }

    public void setLoadEfficiencyTwin(Integer loadEfficiencyTwin) {
        this.loadEfficiencyTwin = loadEfficiencyTwin;
    }

    public Integer getLoadEfficiencyTdm() {
        return loadEfficiencyTdm;
    }

    public void setLoadEfficiencyTdm(Integer loadEfficiencyTdm) {
        this.loadEfficiencyTdm = loadEfficiencyTdm;
    }

    public Double getMoveRangeFrom() {
        return moveRangeFrom;
    }

    public void setMoveRangeFrom(Double moveRangeFrom) {
        this.moveRangeFrom = moveRangeFrom;
    }

    public Double getMoveRangeTo() {
        return moveRangeTo;
    }

    public void setMoveRangeTo(Double moveRangeTo) {
        this.moveRangeTo = moveRangeTo;
    }

    public Double getCraneSafeSpan() {
        return craneSafeSpan;
    }

    public void setCraneSafeSpan(Double craneSafeSpan) {
        this.craneSafeSpan = craneSafeSpan;
    }

    public Integer getCraneSeq() {
        return craneSeq;
    }

    public void setCraneSeq(Integer craneSeq) {
        this.craneSeq = craneSeq;
    }

    public Double getCraneSpeed() {
        return craneSpeed;
    }

    public void setCraneSpeed(Double craneSpeed) {
        this.craneSpeed = craneSpeed;
    }

    public Integer getCraneWidth() {
        return craneWidth;
    }

    public void setCraneWidth(Integer craneWidth) {
        this.craneWidth = craneWidth;
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

    public Integer getCraneMaxCarryWeight() {
        return craneMaxCarryWeight;
    }

    public void setCraneMaxCarryWeight(Integer craneMaxCarryWeight) {
        this.craneMaxCarryWeight = craneMaxCarryWeight;
    }

    public Long getWorkBerthId() {
        return workBerthId;
    }

    public void setWorkBerthId(Long workBerthId) {
        this.workBerthId = workBerthId;
    }

    public String getWorkVesselBay() {
        return workVesselBay;
    }

    public void setWorkVesselBay(String workVesselBay) {
        this.workVesselBay = workVesselBay;
    }

    public String getCraneTypeId() {
        return craneTypeId;
    }

    public void setCraneTypeId(String craneTypeId) {
        this.craneTypeId = craneTypeId;
    }

    public String getTandemFlag() {
        return tandemFlag;
    }

    public void setTandemFlag(String tandemFlag) {
        this.tandemFlag = tandemFlag;
    }
}
