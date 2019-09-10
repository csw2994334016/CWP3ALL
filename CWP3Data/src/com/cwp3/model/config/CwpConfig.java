package com.cwp3.model.config;

/**
 * Created by csw on 2017/4/19 20:04.
 * Explain: CWP算法相关的配置参数
 * 用于单船
 */
public class CwpConfig {

    private Long berthId; //靠泊Id

    //全局参数
    private Long oneCntTime; //所有桥机平均效率，25关/小时
    private Integer twinWeightDiff; //双箱吊工艺的重量差允许范围
    private Double safeDistance; //桥机安全距离，14米
    private Long crossBarTime;//桥机跨机械起趴大梁移动时间，900s
    private Double craneMoveSpeed; //桥机移动速度，0.75m/s
    private Long unlockTwistTime; //甲板上拆锁时间，甲板五层高及以上集装箱拆锁用时，90s

    private Long hatchCoverTimeD; //桥机作业单块舱盖板时间，240s
    private Long single20FootPadTimeD; //单20尺垫脚箱作业用时，180s
    private Long single20SeparateTimeD; //单20尺全隔槽作业用时，180s
    private Long single20HalfSeparateTimeD; //单20尺半隔槽作业用时，180s

    private Long single20TimeAD; //单20尺普通箱作业用时，120s
    private Long single40TimeAD; //单40尺普通箱作业用时，120s
    private Long single45TimeAD; //单45尺普通箱作业用时，120s
    private Long double20TimeAD; //双20尺普通箱作业用时，150s
    private Long double40TimeAD; //双吊具40尺作业用时，140s
    private Long double45TimeAD; //双吊具45尺作业用时，140s
    private Long specialCntTimeAD; //超限箱、分体大件作业用时，360s
    private Long dangerCntTimeAD; //直装直提危险品作业用时，360s
    private Long single20TimeBD; //单20尺普通箱作业用时，120s
    private Long single40TimeBD; //单40尺普通箱作业用时，120s
    private Long single45TimeBD; //单45尺普通箱作业用时，120s
    private Long double20TimeBD; //双20尺普通箱作业用时，150s
    private Long double40TimeBD; //双吊具40尺作业用时，140s
    private Long double45TimeBD; //双吊具45尺作业用时，140s
    private Long specialCntTimeBD; //超限箱、分体大件作业用时，360s
    private Long dangerCntTimeBD; //直装直提危险品作业用时，360s

    private Long hatchCoverTimeL; //桥机作业单块舱盖板时间，240s
    private Long single20FootPadTimeL; //单20尺垫脚箱作业用时，180s
    private Long single20SeparateTimeL; //单20尺全隔槽作业用时，180s
    private Long single20HalfSeparateTimeL; //单20尺半隔槽作业用时，180s

    private Long single20TimeAL; //单20尺普通箱作业用时，120s
    private Long single40TimeAL; //单40尺普通箱作业用时，120s
    private Long single45TimeAL; //单45尺普通箱作业用时，120s
    private Long double20TimeAL; //双20尺普通箱作业用时，150s
    private Long double40TimeAL; //双吊具40尺作业用时，140s
    private Long double45TimeAL; //双吊具45尺作业用时，140s
    private Long specialCntTimeAL; //超限箱、分体大件作业用时，360s
    private Long dangerCntTimeAL; //直装直提危险品作业用时，360s
    private Long single20TimeBL; //单20尺普通箱作业用时，120s
    private Long single40TimeBL; //单40尺普通箱作业用时，120s
    private Long single45TimeBL; //单45尺普通箱作业用时，120s
    private Long double20TimeBL; //双20尺普通箱作业用时，150s
    private Long double40TimeBL; //双吊具40尺作业用时，140s
    private Long double45TimeBL; //双吊具45尺作业用时，140s
    private Long specialCntTimeBL; //超限箱、分体大件作业用时，360s
    private Long dangerCntTimeBL; //直装直提危险品作业用时，360s

    private Long badCntTime; //故障箱处理用时，360s
    private Long hatchScanTime; //桥机换倍船扫时间，300s
    private Double impactFactor; //特殊因素影响效率的影响因子，1.0

    //单船功能参数
    private Boolean setupBridge;//是否过驾驶台
    private Boolean setupChimney;//是否果烟囱
    private String ldStrategy;//装卸策略，即边装边卸：BLD、一般装卸：LD，默认驳船不做边装边卸，大船能做边装边卸就做
    private Integer craneAdviceNumber;//建议开路数
    private String recycleCntWiFlag; //回收指令标记，控制是否回收队列中的指令，Y表示回收，N表示不回收
    private String craneAdviceWorkBayNos; //桥机第一次开路倍位，四部桥机101、102、103、104，开路倍位设置依次为格式："02,10,00,49"倍位之间用","隔开，"00"代表对应桥机不设置开路倍位


    //单船CWP决策参数
    private String loadPrior; //船舶开路装卸平衡考虑参数。首次开路全装、首次开路全卸、首次开路装卸错开:"L"、"D"、"LD"
    private String craneSameWorkTime; //均衡每部桥吊的作业量，整船桥吊同时完工，Y/N
    private String mainRoadOneCrane; //重点路单桥吊持续作业，其余箱量由左右桥吊分配， Y/N
    private String craneNotSameWorkTime; //桥机作业量不平均（中间桥机晚结束，两边桥机早结束，分割舱最少），Y/N
    private String deckWorkLater; //避免开工作业甲板装船箱，避让时间为开工后一小时，Y/N
    private String dividedHatchFirst; //分割舱优先作业设定，Y/N
    private String splitRoad; //劈路原则，Y/N

    // 双吊具控制参数
    private Double allContainerWeight; // 双吊具工艺作业的集装箱合计重量限制，默认<=60000kg可以作业
    private Double tandemWeightDiff; // 双吊具工艺一关作业的两箱重量差限制，默认<=20000kg可作业
    private Double tandemHeightDiff; // 双吊具工艺一关作业的两箱高度差限制，默认<=40cm可作业
    private String tandemContainerType; // 双吊具工艺可以作业的箱型，默认值："GP,HC,OT"。（OT箱带超限标记的不包括在内）
    private String tandemDangerCnt; // 双吊具工艺可以作业危险品箱型类别，默认值："3,6,8,9"

    private String multiThreadFlag; // 是否使用多线程，"Y"表示使用多线程，"N"或者null表示不使用多线程

    public String getCraneAdviceWorkBayNos() {
        return craneAdviceWorkBayNos;
    }

    public void setCraneAdviceWorkBayNos(String craneAdviceWorkBayNos) {
        this.craneAdviceWorkBayNos = craneAdviceWorkBayNos;
    }

    public String getRecycleCntWiFlag() {
        return recycleCntWiFlag;
    }

    public void setRecycleCntWiFlag(String recycleCntWiFlag) {
        this.recycleCntWiFlag = recycleCntWiFlag;
    }

    public String getSplitRoad() {
        return splitRoad;
    }

    public void setSplitRoad(String splitRoad) {
        this.splitRoad = splitRoad;
    }

    public String getCraneNotSameWorkTime() {
        return craneNotSameWorkTime;
    }

    public void setCraneNotSameWorkTime(String craneNotSameWorkTime) {
        this.craneNotSameWorkTime = craneNotSameWorkTime;
    }

    public Long getHatchScanTime() {
        return hatchScanTime;
    }

    public void setHatchScanTime(Long hatchScanTime) {
        this.hatchScanTime = hatchScanTime;
    }

    public Long getOneCntTime() {
        return oneCntTime;
    }

    public void setOneCntTime(Long oneCntTime) {
        this.oneCntTime = oneCntTime;
    }

    public Integer getTwinWeightDiff() {
        return twinWeightDiff;
    }

    public void setTwinWeightDiff(Integer twinWeightDiff) {
        this.twinWeightDiff = twinWeightDiff;
    }

    public Long getBerthId() {
        return berthId;
    }

    public void setBerthId(Long berthId) {
        this.berthId = berthId;
    }

    public Double getSafeDistance() {
        return safeDistance;
    }

    public void setSafeDistance(Double safeDistance) {
        this.safeDistance = safeDistance;
    }

    public Long getCrossBarTime() {
        return crossBarTime;
    }

    public void setCrossBarTime(Long crossBarTime) {
        this.crossBarTime = crossBarTime;
    }

    public Double getCraneMoveSpeed() {
        return craneMoveSpeed;
    }

    public void setCraneMoveSpeed(Double craneMoveSpeed) {
        this.craneMoveSpeed = craneMoveSpeed;
    }

    public Long getUnlockTwistTime() {
        return unlockTwistTime;
    }

    public void setUnlockTwistTime(Long unlockTwistTime) {
        this.unlockTwistTime = unlockTwistTime;
    }

    public Long getHatchCoverTimeD() {
        return hatchCoverTimeD;
    }

    public void setHatchCoverTimeD(Long hatchCoverTimeD) {
        this.hatchCoverTimeD = hatchCoverTimeD;
    }

    public Long getSingle20FootPadTimeD() {
        return single20FootPadTimeD;
    }

    public void setSingle20FootPadTimeD(Long single20FootPadTimeD) {
        this.single20FootPadTimeD = single20FootPadTimeD;
    }

    public Long getSingle20SeparateTimeD() {
        return single20SeparateTimeD;
    }

    public void setSingle20SeparateTimeD(Long single20SeparateTimeD) {
        this.single20SeparateTimeD = single20SeparateTimeD;
    }

    public Long getSingle20HalfSeparateTimeD() {
        return single20HalfSeparateTimeD;
    }

    public void setSingle20HalfSeparateTimeD(Long single20HalfSeparateTimeD) {
        this.single20HalfSeparateTimeD = single20HalfSeparateTimeD;
    }

    public Long getBadCntTime() {
        return badCntTime;
    }

    public void setBadCntTime(Long badCntTime) {
        this.badCntTime = badCntTime;
    }

    public Double getImpactFactor() {
        return impactFactor;
    }

    public void setImpactFactor(Double impactFactor) {
        this.impactFactor = impactFactor;
    }

    public Boolean getSetupBridge() {
        return setupBridge;
    }

    public void setSetupBridge(Boolean setupBridge) {
        this.setupBridge = setupBridge;
    }

    public Boolean getSetupChimney() {
        return setupChimney;
    }

    public void setSetupChimney(Boolean setupChimney) {
        this.setupChimney = setupChimney;
    }

    public String getLdStrategy() {
        return ldStrategy;
    }

    public void setLdStrategy(String ldStrategy) {
        this.ldStrategy = ldStrategy;
    }

    public Integer getCraneAdviceNumber() {
        return craneAdviceNumber;
    }

    public void setCraneAdviceNumber(Integer craneAdviceNumber) {
        this.craneAdviceNumber = craneAdviceNumber;
    }

    public String getLoadPrior() {
        return loadPrior;
    }

    public void setLoadPrior(String loadPrior) {
        this.loadPrior = loadPrior;
    }

    public String getCraneSameWorkTime() {
        return craneSameWorkTime;
    }

    public void setCraneSameWorkTime(String craneSameWorkTime) {
        this.craneSameWorkTime = craneSameWorkTime;
    }

    public String getDeckWorkLater() {
        return deckWorkLater;
    }

    public void setDeckWorkLater(String deckWorkLater) {
        this.deckWorkLater = deckWorkLater;
    }

    public String getMainRoadOneCrane() {
        return mainRoadOneCrane;
    }

    public void setMainRoadOneCrane(String mainRoadOneCrane) {
        this.mainRoadOneCrane = mainRoadOneCrane;
    }

    public String getDividedHatchFirst() {
        return dividedHatchFirst;
    }

    public void setDividedHatchFirst(String dividedHatchFirst) {
        this.dividedHatchFirst = dividedHatchFirst;
    }

    public Long getHatchCoverTimeL() {
        return hatchCoverTimeL;
    }

    public void setHatchCoverTimeL(Long hatchCoverTimeL) {
        this.hatchCoverTimeL = hatchCoverTimeL;
    }

    public Long getSingle20FootPadTimeL() {
        return single20FootPadTimeL;
    }

    public void setSingle20FootPadTimeL(Long single20FootPadTimeL) {
        this.single20FootPadTimeL = single20FootPadTimeL;
    }

    public Long getSingle20SeparateTimeL() {
        return single20SeparateTimeL;
    }

    public void setSingle20SeparateTimeL(Long single20SeparateTimeL) {
        this.single20SeparateTimeL = single20SeparateTimeL;
    }

    public Long getSingle20HalfSeparateTimeL() {
        return single20HalfSeparateTimeL;
    }

    public void setSingle20HalfSeparateTimeL(Long single20HalfSeparateTimeL) {
        this.single20HalfSeparateTimeL = single20HalfSeparateTimeL;
    }

    public Long getSingle20TimeAD() {
        return single20TimeAD;
    }

    public void setSingle20TimeAD(Long single20TimeAD) {
        this.single20TimeAD = single20TimeAD;
    }

    public Long getSingle40TimeAD() {
        return single40TimeAD;
    }

    public void setSingle40TimeAD(Long single40TimeAD) {
        this.single40TimeAD = single40TimeAD;
    }

    public Long getSingle45TimeAD() {
        return single45TimeAD;
    }

    public void setSingle45TimeAD(Long single45TimeAD) {
        this.single45TimeAD = single45TimeAD;
    }

    public Long getDouble20TimeAD() {
        return double20TimeAD;
    }

    public void setDouble20TimeAD(Long double20TimeAD) {
        this.double20TimeAD = double20TimeAD;
    }

    public Long getDouble40TimeAD() {
        return double40TimeAD;
    }

    public void setDouble40TimeAD(Long double40TimeAD) {
        this.double40TimeAD = double40TimeAD;
    }

    public Long getDouble45TimeAD() {
        return double45TimeAD;
    }

    public void setDouble45TimeAD(Long double45TimeAD) {
        this.double45TimeAD = double45TimeAD;
    }

    public Long getSpecialCntTimeAD() {
        return specialCntTimeAD;
    }

    public void setSpecialCntTimeAD(Long specialCntTimeAD) {
        this.specialCntTimeAD = specialCntTimeAD;
    }

    public Long getDangerCntTimeAD() {
        return dangerCntTimeAD;
    }

    public void setDangerCntTimeAD(Long dangerCntTimeAD) {
        this.dangerCntTimeAD = dangerCntTimeAD;
    }

    public Long getSingle20TimeBD() {
        return single20TimeBD;
    }

    public void setSingle20TimeBD(Long single20TimeBD) {
        this.single20TimeBD = single20TimeBD;
    }

    public Long getSingle40TimeBD() {
        return single40TimeBD;
    }

    public void setSingle40TimeBD(Long single40TimeBD) {
        this.single40TimeBD = single40TimeBD;
    }

    public Long getSingle45TimeBD() {
        return single45TimeBD;
    }

    public void setSingle45TimeBD(Long single45TimeBD) {
        this.single45TimeBD = single45TimeBD;
    }

    public Long getDouble20TimeBD() {
        return double20TimeBD;
    }

    public void setDouble20TimeBD(Long double20TimeBD) {
        this.double20TimeBD = double20TimeBD;
    }

    public Long getDouble40TimeBD() {
        return double40TimeBD;
    }

    public void setDouble40TimeBD(Long double40TimeBD) {
        this.double40TimeBD = double40TimeBD;
    }

    public Long getDouble45TimeBD() {
        return double45TimeBD;
    }

    public void setDouble45TimeBD(Long double45TimeBD) {
        this.double45TimeBD = double45TimeBD;
    }

    public Long getSpecialCntTimeBD() {
        return specialCntTimeBD;
    }

    public void setSpecialCntTimeBD(Long specialCntTimeBD) {
        this.specialCntTimeBD = specialCntTimeBD;
    }

    public Long getDangerCntTimeBD() {
        return dangerCntTimeBD;
    }

    public void setDangerCntTimeBD(Long dangerCntTimeBD) {
        this.dangerCntTimeBD = dangerCntTimeBD;
    }

    public Long getSingle20TimeAL() {
        return single20TimeAL;
    }

    public void setSingle20TimeAL(Long single20TimeAL) {
        this.single20TimeAL = single20TimeAL;
    }

    public Long getSingle40TimeAL() {
        return single40TimeAL;
    }

    public void setSingle40TimeAL(Long single40TimeAL) {
        this.single40TimeAL = single40TimeAL;
    }

    public Long getSingle45TimeAL() {
        return single45TimeAL;
    }

    public void setSingle45TimeAL(Long single45TimeAL) {
        this.single45TimeAL = single45TimeAL;
    }

    public Long getDouble20TimeAL() {
        return double20TimeAL;
    }

    public void setDouble20TimeAL(Long double20TimeAL) {
        this.double20TimeAL = double20TimeAL;
    }

    public Long getDouble40TimeAL() {
        return double40TimeAL;
    }

    public void setDouble40TimeAL(Long double40TimeAL) {
        this.double40TimeAL = double40TimeAL;
    }

    public Long getDouble45TimeAL() {
        return double45TimeAL;
    }

    public void setDouble45TimeAL(Long double45TimeAL) {
        this.double45TimeAL = double45TimeAL;
    }

    public Long getSpecialCntTimeAL() {
        return specialCntTimeAL;
    }

    public void setSpecialCntTimeAL(Long specialCntTimeAL) {
        this.specialCntTimeAL = specialCntTimeAL;
    }

    public Long getDangerCntTimeAL() {
        return dangerCntTimeAL;
    }

    public void setDangerCntTimeAL(Long dangerCntTimeAL) {
        this.dangerCntTimeAL = dangerCntTimeAL;
    }

    public Long getSingle20TimeBL() {
        return single20TimeBL;
    }

    public void setSingle20TimeBL(Long single20TimeBL) {
        this.single20TimeBL = single20TimeBL;
    }

    public Long getSingle40TimeBL() {
        return single40TimeBL;
    }

    public void setSingle40TimeBL(Long single40TimeBL) {
        this.single40TimeBL = single40TimeBL;
    }

    public Long getSingle45TimeBL() {
        return single45TimeBL;
    }

    public void setSingle45TimeBL(Long single45TimeBL) {
        this.single45TimeBL = single45TimeBL;
    }

    public Long getDouble20TimeBL() {
        return double20TimeBL;
    }

    public void setDouble20TimeBL(Long double20TimeBL) {
        this.double20TimeBL = double20TimeBL;
    }

    public Long getDouble40TimeBL() {
        return double40TimeBL;
    }

    public void setDouble40TimeBL(Long double40TimeBL) {
        this.double40TimeBL = double40TimeBL;
    }

    public Long getDouble45TimeBL() {
        return double45TimeBL;
    }

    public void setDouble45TimeBL(Long double45TimeBL) {
        this.double45TimeBL = double45TimeBL;
    }

    public Long getSpecialCntTimeBL() {
        return specialCntTimeBL;
    }

    public void setSpecialCntTimeBL(Long specialCntTimeBL) {
        this.specialCntTimeBL = specialCntTimeBL;
    }

    public Long getDangerCntTimeBL() {
        return dangerCntTimeBL;
    }

    public void setDangerCntTimeBL(Long dangerCntTimeBL) {
        this.dangerCntTimeBL = dangerCntTimeBL;
    }

    public Double getAllContainerWeight() {
        return allContainerWeight;
    }

    public void setAllContainerWeight(Double allContainerWeight) {
        this.allContainerWeight = allContainerWeight;
    }

    public Double getTandemWeightDiff() {
        return tandemWeightDiff;
    }

    public void setTandemWeightDiff(Double tandemWeightDiff) {
        this.tandemWeightDiff = tandemWeightDiff;
    }

    public Double getTandemHeightDiff() {
        return tandemHeightDiff;
    }

    public void setTandemHeightDiff(Double tandemHeightDiff) {
        this.tandemHeightDiff = tandemHeightDiff;
    }

    public String getTandemContainerType() {
        return tandemContainerType;
    }

    public void setTandemContainerType(String tandemContainerType) {
        this.tandemContainerType = tandemContainerType;
    }

    public String getTandemDangerCnt() {
        return tandemDangerCnt;
    }

    public void setTandemDangerCnt(String tandemDangerCnt) {
        this.tandemDangerCnt = tandemDangerCnt;
    }

    public String getMultiThreadFlag() {
        return multiThreadFlag;
    }

    public void setMultiThreadFlag(String multiThreadFlag) {
        this.multiThreadFlag = multiThreadFlag;
    }
}
