package com.cwp3.single.data;

import com.cwp3.data.single.StructureData;
import com.cwp3.data.single.WorkingData;
import com.cwp3.single.algorithm.cwp.method.PublicMethod;
import com.cwp3.single.algorithm.cwp.modal.*;
import com.cwp3.utils.DateUtil;

import java.util.*;

/**
 * Created by csw on 2018/5/30.
 * Description:
 */
public class CwpData {

    private WorkingData workingData;
    private StructureData structureData;

    private Map<Integer, CWPBay> cwpBayMap; // 深复制
    private Map<Integer, CWPBay> machineBayMap; // 深复制
    private Map<String, CWPCrane> cwpCraneMap; // 深复制
    private Long cwpStartTime;

    private MoveData moveData; // 深复制
    private MoveResults moveResults; // 深复制

    private Boolean firstDoCwp;
    private Long dpCurrentTime;
    private DPResult dpResult;
    private List<CWPCrane> dpCwpCraneList; // 深复制
    private List<DPCraneSelectBay> dpCraneSelectBays;
    private String dpStrategyType;
    private Integer dpMoveNumber;
    private String dpInvalidateBranch; // 超过船期无效的分支
    private String dpExceptionBranch; // 发生异常的分支

    private Map<Integer, List<CWPBay>> cwpHatchBayMap; // <bayNoD, 一般3个倍位>，每次analyzeCwpBay方法都会重新计算
    private Map<String, List<CWPCrane>> dpFirstCwpCraneMap; // Analyzer->Evaluator
    private Long evaluateTime;
    private Integer dpChangeHatchNumber; // 换舱次数

    public CwpData(WorkingData workingData, StructureData structureData) {
        this.workingData = workingData;
        this.structureData = structureData;
        cwpBayMap = new HashMap<>();
        cwpHatchBayMap = new HashMap<>();
        machineBayMap = new HashMap<>();
        cwpCraneMap = new HashMap<>();
        moveResults = new MoveResults();
        firstDoCwp = Boolean.TRUE;
        dpCwpCraneList = new ArrayList<>();
        dpResult = new DPResult();
        dpCraneSelectBays = new ArrayList<>();
        dpFirstCwpCraneMap = new LinkedHashMap<>();
        dpMoveNumber = 0;
        dpChangeHatchNumber = 0;
    }

    public WorkingData getWorkingData() {
        return workingData;
    }

    public StructureData getStructureData() {
        return structureData;
    }

    public Long getVesselTime() {
        return DateUtil.getSecondTime(workingData.getVmSchedule().getPlanEndWorkTime()) - dpCurrentTime - 3600;
    }

    public Long getCwpStartTime() {
        return cwpStartTime;
    }

    public void setCwpStartTime(Long cwpStartTime) {
        this.cwpStartTime = cwpStartTime;
    }

    public void addCWPCrane(CWPCrane cwpCrane) {
        cwpCraneMap.put(cwpCrane.getCraneNo(), cwpCrane);
    }

    public CWPCrane getCWPCraneByCraneNo(String craneNo) {
        return cwpCraneMap.get(craneNo);
    }

    public List<CWPCrane> getAllCWPCranes() {
        List<CWPCrane> cwpCraneList = new ArrayList<>(cwpCraneMap.values());
        PublicMethod.sortCwpCraneByCraneSeq(cwpCraneList);
        return cwpCraneList;
    }

    public void addCWPBay(CWPBay cwpBay) {
        cwpBayMap.put(cwpBay.getBayNo(), cwpBay);
    }

    public CWPBay getCWPBayByBayNo(Integer bayNo) {
        return cwpBayMap.get(bayNo);
    }

    public List<CWPBay> getAllCWPBays() {
        List<CWPBay> cwpBayList = new ArrayList<>(cwpBayMap.values());
        PublicMethod.sortCwpBayByWorkPosition(cwpBayList);
        return cwpBayList;
    }

    public Map<Integer, List<CWPBay>> getCwpHatchBayMap() {
        return cwpHatchBayMap;
    }

    public void setCwpHatchBayMap(Map<Integer, List<CWPBay>> cwpHatchBayMap) {
        this.cwpHatchBayMap = cwpHatchBayMap;
    }

    public List<CWPBay> getCwpBayListByBayNoD(Integer bayNoD) {
        return cwpHatchBayMap.get(bayNoD);
    }

    public void addMachineBay(CWPBay cwpBay) {
        machineBayMap.put(cwpBay.getBayNo(), cwpBay);
    }

    public CWPBay getMachineBayByBayNo(Integer bayNo) {
        return machineBayMap.get(bayNo);
    }

    public List<CWPBay> getAllMachineBays() {
        List<CWPBay> machineBayList = new ArrayList<>(machineBayMap.values());
        PublicMethod.sortCwpBayByWorkPosition(machineBayList);
        return machineBayList;
    }

    public MoveData getMoveData() {
        return moveData;
    }

    public void setMoveData(MoveData moveData) {
        this.moveData = moveData;
    }

    public MoveResults getMoveResults() {
        return moveResults;
    }

    public void setMoveResults(MoveResults moveResults) {
        this.moveResults = moveResults;
    }

    public List<DPCraneSelectBay> getDpCraneSelectBays() {
        return dpCraneSelectBays;
    }

    public void setDpCraneSelectBays(List<DPCraneSelectBay> dpCraneSelectBays) {
        this.dpCraneSelectBays = dpCraneSelectBays;
    }

    public Long getDpCurrentTime() {
        return dpCurrentTime;
    }

    public void setDpCurrentTime(Long dpCurrentTime) {
        this.dpCurrentTime = dpCurrentTime;
    }

    public DPResult getDpResult() {
        return dpResult;
    }

    public void setDpResult(DPResult dpResult) {
        this.dpResult = dpResult;
    }

    public List<CWPCrane> getDpCwpCraneList() {
        return dpCwpCraneList;
    }

    public void setDpCwpCraneList(List<CWPCrane> dpCwpCraneList) {
        this.dpCwpCraneList = dpCwpCraneList;
    }

    public Boolean getFirstDoCwp() {
        return firstDoCwp;
    }

    public void setFirstDoCwp(Boolean firstDoCwp) {
        this.firstDoCwp = firstDoCwp;
    }

    public Map<String, List<CWPCrane>> getDpFirstCwpCraneMap() {
        return dpFirstCwpCraneMap;
    }

    public void setDpFirstCwpCraneMap(Map<String, List<CWPCrane>> dpFirstCwpCraneMap) {
        this.dpFirstCwpCraneMap = dpFirstCwpCraneMap;
    }

    public Integer getDpMoveNumber() {
        return dpMoveNumber;
    }

    public void setDpMoveNumber(Integer dpMoveNumber) {
        this.dpMoveNumber = dpMoveNumber;
    }

    public String getDpStrategyType() {
        return dpStrategyType;
    }

    public void setDpStrategyType(String dpStrategyType) {
        this.dpStrategyType = dpStrategyType;
    }

    public String getDpInvalidateBranch() {
        return dpInvalidateBranch;
    }

    public void setDpInvalidateBranch(String dpInvalidateBranch) {
        this.dpInvalidateBranch = dpInvalidateBranch;
    }

    public Long getEvaluateTime() {
        return evaluateTime;
    }

    public void setEvaluateTime(Long evaluateTime) {
        this.evaluateTime = evaluateTime;
    }

    public String getDpExceptionBranch() {
        return dpExceptionBranch;
    }

    public void setDpExceptionBranch(String dpExceptionBranch) {
        this.dpExceptionBranch = dpExceptionBranch;
    }

    public Integer getDpChangeHatchNumber() {
        return dpChangeHatchNumber;
    }

    public void setDpChangeHatchNumber(Integer dpChangeHatchNumber) {
        this.dpChangeHatchNumber = dpChangeHatchNumber;
    }
}
