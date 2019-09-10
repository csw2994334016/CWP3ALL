package com.cwp3.model.vessel;


/**
 * Created by csw on 2017/4/20 13:55.
 * Explain: 舱盖板信息
 */
public class VMHatchCover {

    private String vLocation; //舱盖板位置，自己定义成船箱位："大倍号0149"
    private Long hatchId;
    private Integer bayNoFrom; //开始倍位号
    private Integer bayNoTo; //结束倍位号
    private Integer hatchCoverNo;//舱盖板编号
    private Integer openSeq;//开舱顺序 （暂不考虑）
    private String hatchCoverState; //舱盖板状态：打开(open)；关闭(closed)；
    private Integer hatchFromRowNo;//舱内开始排
    private Integer hatchToRowNo; //舱内结束排
    private Integer deckFromRowNo;//甲板上开始排
    private Integer deckToRowNo;// 甲板上结束排
    private Long leftCoverFather;// 左边父甲板编号
    private Long rightCoverFather; // 右边父甲板编号
    private Long frontCoverFather; // 前边父甲板编号
    private Long behindCoverFather; // 后边父甲板编号
    private Double weight; // 舱盖板重量，单位：kg

    public VMHatchCover() {}

    public VMHatchCover(String vLocation, Long hatchId) {
        this.vLocation = vLocation;
        this.hatchId  = hatchId;
    }

    public Long getHatchId() {
        return hatchId;
    }

    public void setHatchId(Long hatchId) {
        this.hatchId = hatchId;
    }

    public String getvLocation() {
        return vLocation;
    }

    public void setvLocation(String vLocation) {
        this.vLocation = vLocation;
    }

    public Integer getBayNoFrom() {
        return bayNoFrom;
    }

    public void setBayNoFrom(Integer bayNoFrom) {
        this.bayNoFrom = bayNoFrom;
    }

    public Integer getBayNoTo() {
        return bayNoTo;
    }

    public void setBayNoTo(Integer bayNoTo) {
        this.bayNoTo = bayNoTo;
    }

    public Integer getHatchCoverNo() {
        return hatchCoverNo;
    }

    public void setHatchCoverNo(Integer hatchCoverNo) {
        this.hatchCoverNo = hatchCoverNo;
    }

    public Integer getOpenSeq() {
        return openSeq;
    }

    public void setOpenSeq(Integer openSeq) {
        this.openSeq = openSeq;
    }

    public Integer getHatchFromRowNo() {
        return hatchFromRowNo;
    }

    public void setHatchFromRowNo(Integer hatchFromRowNo) {
        this.hatchFromRowNo = hatchFromRowNo;
    }

    public Integer getHatchToRowNo() {
        return hatchToRowNo;
    }

    public void setHatchToRowNo(Integer hatchToRowNo) {
        this.hatchToRowNo = hatchToRowNo;
    }

    public Integer getDeckFromRowNo() {
        return deckFromRowNo;
    }

    public void setDeckFromRowNo(Integer deckFromRowNo) {
        this.deckFromRowNo = deckFromRowNo;
    }

    public Integer getDeckToRowNo() {
        return deckToRowNo;
    }

    public void setDeckToRowNo(Integer deckToRowNo) {
        this.deckToRowNo = deckToRowNo;
    }

    public Long getLeftCoverFather() {
        return leftCoverFather;
    }

    public void setLeftCoverFather(Long leftCoverFather) {
        this.leftCoverFather = leftCoverFather;
    }

    public Long getRightCoverFather() {
        return rightCoverFather;
    }

    public void setRightCoverFather(Long rightCoverFather) {
        this.rightCoverFather = rightCoverFather;
    }

    public Long getFrontCoverFather() {
        return frontCoverFather;
    }

    public void setFrontCoverFather(Long frontCoverFather) {
        this.frontCoverFather = frontCoverFather;
    }

    public Long getBehindCoverFather() {
        return behindCoverFather;
    }

    public void setBehindCoverFather(Long behindCoverFather) {
        this.behindCoverFather = behindCoverFather;
    }

    public String getHatchCoverState() {
        return hatchCoverState;
    }

    public void setHatchCoverState(String hatchCoverState) {
        this.hatchCoverState = hatchCoverState;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
