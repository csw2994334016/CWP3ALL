package com.cwp3.single.algorithm.cwp.modal;


import com.cwp3.domain.CWPDomain;

import java.io.*;

/**
 * Created by csw on 2017/9/19.
 * Description: 倍位作业信息(一般分三个倍位置作业)
 */
public class CWPBay implements Serializable {

    private Integer bayNo; //倍位号
    private Long hatchId; //舱Id
    private Double workPosition; //倍位中心位置，即桥机在该倍位作业位置
    private String bayType; //驾驶台当作一个舱，三个倍位看待

    //DP过程中，倍位作业量动态信息
    private Long dpTotalWorkTime; //该倍位作业时间总量，初始化时的量
    private Long dpCurrentTotalWorkTime; //该倍位当前时刻，剩余作业时间总量
    private Long dpAvailableWorkTime; //该倍位当前时刻，可以作业的时间量

    //分析倍位特征
    private Boolean dpSteppingCntFlag; //判断可作业量是全部的垫脚箱，则为true
    private Long dpSteppingAvailableWt;
    private Long dpSteppingTotalWt;
    private Long dpAvailableDiscWtD; // 大倍位卸船可作业量
    private Long dpAvailableLoadWtD; // 大倍位装船可作业量
    private Long dpAvailableDiscWtX; // 小倍位卸船可作业量
    private Long dpAvailableLoadWtX; // 小倍位装船可作业量
    private String dpLoadOrDisc;
    private Long reStowCntTimeD; // 卸船出翻舱在这个倍位开始作业的时间
    private Long reStowCntTimeL; // 卸船出翻舱在这个倍位开始作业的时间

    private String dpSelectedByCraneNo; // 倍位被某部桥机指定要作业
    private String dpSelectedByCraneTrue; // 为"Y"表示可以继续在该舱作业

    private String dpLockByCraneNo; // 当前决策时，该倍位被哪部桥机锁定了

    public CWPBay(Integer bayNo, Long hatchId, Double workPosition) {
        this.bayNo = bayNo;
        this.hatchId = hatchId;
        this.workPosition = workPosition;
        bayType = CWPDomain.BAY_TYPE_NATURAL;
        dpTotalWorkTime = 0L;
        dpCurrentTotalWorkTime = 0L;
        dpAvailableWorkTime = 0L;
        dpSteppingCntFlag = Boolean.FALSE;
        reStowCntTimeD = 0L;
        reStowCntTimeL = 0L;
    }

    @Override
    public String toString() {
        return "CWPBay{" +
                "bayNo=" + bayNo +
                '}';
    }

    public Integer getBayNo() {
        return bayNo;
    }

    public Long getHatchId() {
        return hatchId;
    }

    public Double getWorkPosition() {
        return workPosition;
    }

    public String getBayType() {
        return bayType;
    }

    public void setBayType(String bayType) {
        this.bayType = bayType;
    }

    public Long getDpTotalWorkTime() {
        return dpTotalWorkTime;
    }

    public void setDpTotalWorkTime(Long dpTotalWorkTime) {
        this.dpTotalWorkTime = dpTotalWorkTime;
    }

    public void addDpTotalWorkTime(Long dpTotalWorkTime) {
        this.dpTotalWorkTime += dpTotalWorkTime;
    }

    public Long getDpCurrentTotalWorkTime() {
        return dpCurrentTotalWorkTime;
    }

    public void setDpCurrentTotalWorkTime(Long dpCurrentTotalWorkTime) {
        this.dpCurrentTotalWorkTime = dpCurrentTotalWorkTime;
    }

    public Long getDpAvailableWorkTime() {
        return dpAvailableWorkTime;
    }

    public void setDpAvailableWorkTime(Long dpAvailableWorkTime) {
        this.dpAvailableWorkTime = dpAvailableWorkTime;
    }

    public Boolean getDpSteppingCntFlag() {
        return dpSteppingCntFlag;
    }

    public void setDpSteppingCntFlag(Boolean dpSteppingCntFlag) {
        this.dpSteppingCntFlag = dpSteppingCntFlag;
    }

    public Long getDpSteppingAvailableWt() {
        return dpSteppingAvailableWt;
    }

    public void setDpSteppingAvailableWt(Long dpSteppingAvailableWt) {
        this.dpSteppingAvailableWt = dpSteppingAvailableWt;
    }

    public Long getDpSteppingTotalWt() {
        return dpSteppingTotalWt;
    }

    public void setDpSteppingTotalWt(Long dpSteppingTotalWt) {
        this.dpSteppingTotalWt = dpSteppingTotalWt;
    }

    public Long getDpAvailableDiscWtD() {
        return dpAvailableDiscWtD;
    }

    public void setDpAvailableDiscWtD(Long dpAvailableDiscWtD) {
        this.dpAvailableDiscWtD = dpAvailableDiscWtD;
    }

    public Long getDpAvailableLoadWtD() {
        return dpAvailableLoadWtD;
    }

    public void setDpAvailableLoadWtD(Long dpAvailableLoadWtD) {
        this.dpAvailableLoadWtD = dpAvailableLoadWtD;
    }

    public String getDpLoadOrDisc() {
        return dpLoadOrDisc;
    }

    public void setDpLoadOrDisc(String dpLoadOrDisc) {
        this.dpLoadOrDisc = dpLoadOrDisc;
    }

    public String getDpSelectedByCraneNo() {
        return dpSelectedByCraneNo;
    }

    public void setDpSelectedByCraneNo(String dpSelectedByCraneNo) {
        this.dpSelectedByCraneNo = dpSelectedByCraneNo;
    }

    public Long getReStowCntTimeD() {
        return reStowCntTimeD;
    }

    public void setReStowCntTimeD(Long reStowCntTimeD) {
        this.reStowCntTimeD = reStowCntTimeD;
    }

    public Long getReStowCntTimeL() {
        return reStowCntTimeL;
    }

    public void setReStowCntTimeL(Long reStowCntTimeL) {
        this.reStowCntTimeL = reStowCntTimeL;
    }

    public Long getDpAvailableDiscWtX() {
        return dpAvailableDiscWtX;
    }

    public void setDpAvailableDiscWtX(Long dpAvailableDiscWtX) {
        this.dpAvailableDiscWtX = dpAvailableDiscWtX;
    }

    public Long getDpAvailableLoadWtX() {
        return dpAvailableLoadWtX;
    }

    public void setDpAvailableLoadWtX(Long dpAvailableLoadWtX) {
        this.dpAvailableLoadWtX = dpAvailableLoadWtX;
    }

    public String getDpSelectedByCraneTrue() {
        return dpSelectedByCraneTrue;
    }

    public String getDpLockByCraneNo() {
        return dpLockByCraneNo;
    }

    public void setDpLockByCraneNo(String dpLockByCraneNo) {
        this.dpLockByCraneNo = dpLockByCraneNo;
    }

    public void setDpSelectedByCraneTrue(String dpSelectedByCraneTrue) {
        this.dpSelectedByCraneTrue = dpSelectedByCraneTrue;
    }

    public CWPBay deepCopy() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(this);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);

            return (CWPBay) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}