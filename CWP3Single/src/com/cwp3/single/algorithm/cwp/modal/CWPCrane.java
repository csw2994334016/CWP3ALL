package com.cwp3.single.algorithm.cwp.modal;

import java.io.*;
import java.util.*;

/**
 * Created by csw on 2017/9/19.
 * Description:
 */
public class CWPCrane implements Serializable {

    private String craneNo; //桥机号，主键

    //桥机基础信息
    private Double currentCranePosition; //当前位置
    private Double moveRangeFrom; //移动范围起始点
    private Double moveRangeTo; //移动范围终止点
    private Integer moveRangeBayNoFrom; // 移动范围起始倍位
    private Integer moveRangeBayNoTo; // 移动范围终止倍位
    private Double craneSafeSpan; //安全距离
    private Integer craneSeq; //桥吊序列号
    private Double craneSpeed; //移动速度，米/分钟
    private Integer craneWidth; //桥吊宽度
    private String craneMaxCarryWeight; //桥吊的最大作业吨位
    private String tandemFlag; // 双吊具工艺标志：Y表示桥机可以做双吊具工艺；N或者null表示桥机不可以做双吊具工艺

    //人工设置的桥机相关参数
    private String craneStatus; //桥吊作业状态故障
    private String craneMoveStatus; //是否可以移动
    private Long workBerthId; //靠泊ID
    private String workVesselBay; //当前作业倍位
    private Integer firstWorkBayNo; //开始作业的倍位
    private Long firstWorkAmount; //在这个倍位作业的量，关数

    // DP过程中的动态信息
    private Double dpCurrentWorkPosition; //桥机当前所在作业位置
    private Integer dpCurrentWorkBayNo; //桥机当前作业哪个倍位(每个舱三个作业位置)
    private Long dpCurrentTime; //桥机当前时刻计划作业时间(秒)，绝对时间
    private Long dpEndWorkTime; // 每次决策桥机计划结束作业的时间

    // DP桥机作业范围标识
    private Long dpCurMeanWt; //当前桥机剩余的平均作业量
    private Integer dpWorkBayNoFrom;//桥机分块范围起始倍位号
    private Integer dpWorkBayNoTo;//桥机分块范围终止位置倍位号
    private Long dpWorkTimeFrom;//桥机分块在起始位置的作业量
    private Long dpWorkTimeTo;//桥机分块在终止位置的作业量
    private LinkedList<Integer> dpFirstCanSelectBays; //桥机第一次平均量划分可以选择作业的倍位

    // 每次DP之前进行分析的中间结果
    private LinkedList<Integer> dpCurCanSelectBays; //桥机当前可以选择作业的倍位
    private Set<Integer> dpSelectBays; //桥机决策可以选择作业的倍位
    private Boolean dpWait; //桥机等待不作业状态

    private List<CWPCraneWork> cwpCraneWorkList; // 加减桥机计划
    private Long craneWorkTime; // 加减的桥机只作业这么多量
    private String addOrDelType; // 经过加减桥机时间分析，只下路一次（包括中途上路的）、只上路一次就没下路

    private Long dpWorkCntAmount; // 桥机作业的指令量

    public CWPCrane(String craneNo) {
        this.craneNo = craneNo;
        dpCurrentTime = 0L;
        dpCurMeanWt = 0L;
        dpWorkTimeFrom = 0L;
        dpWorkTimeTo = 0L;
        dpFirstCanSelectBays = new LinkedList<>();
        dpCurCanSelectBays = new LinkedList<>();
        dpSelectBays = new HashSet<>();
        dpWait = Boolean.FALSE;
        cwpCraneWorkList = new ArrayList<>();
        craneWorkTime = 0L;
        dpWorkCntAmount = 0L;
    }

    @Override
    public String toString() {
        return "CWPCrane{" +
                "craneNo='" + craneNo + '\'' +
                '}';
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

    public String getCraneMaxCarryWeight() {
        return craneMaxCarryWeight;
    }

    public void setCraneMaxCarryWeight(String craneMaxCarryWeight) {
        this.craneMaxCarryWeight = craneMaxCarryWeight;
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

    public Double getDpCurrentWorkPosition() {
        return dpCurrentWorkPosition;
    }

    public void setDpCurrentWorkPosition(Double dpCurrentWorkPosition) {
        this.dpCurrentWorkPosition = dpCurrentWorkPosition;
    }

    public Integer getDpCurrentWorkBayNo() {
        return dpCurrentWorkBayNo;
    }

    public void setDpCurrentWorkBayNo(Integer dpCurrentWorkBayNo) {
        this.dpCurrentWorkBayNo = dpCurrentWorkBayNo;
    }

    public Long getDpCurrentTime() {
        return dpCurrentTime;
    }

    public void setDpCurrentTime(Long dpCurrentTime) {
        this.dpCurrentTime = dpCurrentTime;
    }

    public void addDpCurrentTime(long moveTime) {
        this.dpCurrentTime += moveTime;
    }

    public Long getDpEndWorkTime() {
        return dpEndWorkTime;
    }

    public void setDpEndWorkTime(Long dpEndWorkTime) {
        this.dpEndWorkTime = dpEndWorkTime;
    }

    public LinkedList<Integer> getDpFirstCanSelectBays() {
        return dpFirstCanSelectBays;
    }

    public void setDpFirstCanSelectBays(LinkedList<Integer> dpFirstCanSelectBays) {
        this.dpFirstCanSelectBays = dpFirstCanSelectBays;
    }

    public LinkedList<Integer> getDpCurCanSelectBays() {
        return dpCurCanSelectBays;
    }

    public void setDpCurCanSelectBays(LinkedList<Integer> dpCurCanSelectBays) {
        this.dpCurCanSelectBays = dpCurCanSelectBays;
    }

    public Long getDpCurMeanWt() {
        return dpCurMeanWt;
    }

    public void setDpCurMeanWt(Long dpCurMeanWt) {
        this.dpCurMeanWt = dpCurMeanWt;
    }

    public void addDpCurMeanWt(Long dpCurMeanWorkTime) {
        this.dpCurMeanWt += dpCurMeanWorkTime;
    }

    public Boolean getDpWait() {
        return dpWait;
    }

    public void setDpWait(Boolean dpWait) {
        this.dpWait = dpWait;
    }

    public Set<Integer> getDpSelectBays() {
        return dpSelectBays;
    }

    public void setDpSelectBays(Set<Integer> dpSelectBays) {
        this.dpSelectBays = dpSelectBays;
    }

    public Integer getDpWorkBayNoFrom() {
        return dpWorkBayNoFrom;
    }

    public void setDpWorkBayNoFrom(Integer dpWorkBayNoFrom) {
        this.dpWorkBayNoFrom = dpWorkBayNoFrom;
    }

    public Integer getDpWorkBayNoTo() {
        return dpWorkBayNoTo;
    }

    public void setDpWorkBayNoTo(Integer dpWorkBayNoTo) {
        this.dpWorkBayNoTo = dpWorkBayNoTo;
    }

    public Long getDpWorkTimeFrom() {
        return dpWorkTimeFrom;
    }

    public void setDpWorkTimeFrom(Long dpWorkTimeFrom) {
        this.dpWorkTimeFrom = dpWorkTimeFrom;
    }

    public Long getDpWorkTimeTo() {
        return dpWorkTimeTo;
    }

    public void setDpWorkTimeTo(Long dpWorkTimeTo) {
        this.dpWorkTimeTo = dpWorkTimeTo;
    }

    public List<CWPCraneWork> getCwpCraneWorkList() {
        return cwpCraneWorkList;
    }

    public void setCwpCraneWorkList(List<CWPCraneWork> cwpCraneWorkList) {
        this.cwpCraneWorkList = cwpCraneWorkList;
    }

    public Long getCraneWorkTime() {
        return craneWorkTime;
    }

    public void setCraneWorkTime(Long craneWorkTime) {
        this.craneWorkTime = craneWorkTime;
    }

    public Integer getMoveRangeBayNoFrom() {
        return moveRangeBayNoFrom;
    }

    public void setMoveRangeBayNoFrom(Integer moveRangeBayNoFrom) {
        this.moveRangeBayNoFrom = moveRangeBayNoFrom;
    }

    public Integer getMoveRangeBayNoTo() {
        return moveRangeBayNoTo;
    }

    public void setMoveRangeBayNoTo(Integer moveRangeBayNoTo) {
        this.moveRangeBayNoTo = moveRangeBayNoTo;
    }

    public String getAddOrDelType() {
        return addOrDelType;
    }

    public void setAddOrDelType(String addOrDelType) {
        this.addOrDelType = addOrDelType;
    }

    public String getTandemFlag() {
        return tandemFlag;
    }

    public void setTandemFlag(String tandemFlag) {
        this.tandemFlag = tandemFlag;
    }

    public Long getDpWorkCntAmount() {
        return dpWorkCntAmount;
    }

    public void setDpWorkCntAmount(Long dpWorkCntAmount) {
        this.dpWorkCntAmount = dpWorkCntAmount;
    }

    public CWPCrane deepCopy() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(this);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);

            CWPCrane cwpCrane = (CWPCrane) ois.readObject();
            // 深复制一下加减桥机信息
            List<CWPCraneWork> cwpCraneWorkList = new ArrayList<>();
            for (CWPCraneWork cwpCraneWork : this.cwpCraneWorkList) {
                cwpCraneWorkList.add(cwpCraneWork.deepCopy());
            }
            cwpCrane.setCwpCraneWorkList(cwpCraneWorkList);
            return cwpCrane;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
