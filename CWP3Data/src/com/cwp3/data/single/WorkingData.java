package com.cwp3.data.single;

import com.cwp3.domain.CWPCraneDomain;
import com.cwp3.domain.CWPDomain;
import com.cwp3.model.config.CwpConfig;
import com.cwp3.model.crane.*;
import com.cwp3.model.log.Logger;
import com.cwp3.model.vessel.*;
import com.cwp3.model.work.*;
import com.cwp3.utils.CalculateUtil;

import java.util.*;

public class WorkingData {

    private Logger logger; // 日志对象

    //船期信息
    private VMSchedule vmSchedule;
    private String cwpType;

    //单船算法配置参数
    private CwpConfig cwpConfig;

    //传入算法的相关数据
    private Map<String, CMCraneWorkFlow> cmCraneWorkFlowMap; //舱作业工艺配置：<hatchId@aboveOrBelow@dlType, CMCraneWorkFlow>
    private List<VMContainer> sentContainerList; //状态为W、A的已发送箱指令，分析是否需要回收指令用
    private Map<String, VMContainer> discContainerMap; //进出口船图箱信息：<vLocation, VMContainer> 小位置，02需要拆成01和03
    private Map<String, VMContainer> loadContainerMap;
    private List<VMContainerAmount> vmContainerAmountList; //进出口船图箱量信息

    // 过境箱数据
    private Map<String, VMContainer> throughContainerMap; //过境箱信息：<vLocation, VMContainer> 小位置，02需要拆成01和03
    private Map<String, VMContainer> reStowContainerMapD; // 出翻舱，卸船：<yardContainerId, VMContainer>
    private Map<String, VMContainer> reStowContainerMapL; // 出翻舱，装船：<yardContainerId, VMContainer>

    //中间算法分析计算生成的数据
    private List<VMContainer> continueCntList; //指令回收分析过后，按原计划执行，不回收的指令
    private List<VMContainer> recycleCntList; //指令回收分析过后，回收重新安排CWP计划的指令
    private Map<Long, HatchBlock> hatchBlockMap; //分档信息,key: hatchId
    private Map<String, WorkMove> discWorkMoveMap; //卸船Move信息，只会包含单吊具的move，key: vLocation 小位置，02需要拆成01和03
    private Map<String, WorkMove> loadWorkMoveMap; //装船Move信息,key

    //多船算法分配的桥机信息、或人工分配船舶桥机池信息
    private CMCraneManual cmCraneManual;
    private List<CMCranePool> cmCranePoolList;
    private List<CMCraneMaintainPlan> cmCraneMaintainPlanList;
    private List<CMCraneAddOrDelete> cmCraneAddOrDeleteList;
    private Map<String, CMCraneMoveRange> cmCraneMoveRangeMap;

    //CWP计划结果
    private Map<String, List<WorkMove>> workMoveMap;
    private Map<String, List<WorkBlock>> workBlockMap;
    private Map<String, List<CraneEfficiency>> craneEfficiencyMap;
    private Map<String, List<AreaTask>> areaTaskMap;

    public WorkingData(VMSchedule vmSchedule) {
        this.logger = new Logger();
        this.vmSchedule = vmSchedule;
        this.cmCraneWorkFlowMap = new HashMap<>();
        this.sentContainerList = new ArrayList<>();
        this.discContainerMap = new HashMap<>();
        this.loadContainerMap = new HashMap<>();
        this.vmContainerAmountList = new ArrayList<>();
        this.continueCntList = new ArrayList<>();
        this.recycleCntList = new ArrayList<>();
        this.hatchBlockMap = new HashMap<>();
        this.discWorkMoveMap = new HashMap<>();
        this.loadWorkMoveMap = new HashMap<>();
        this.cmCranePoolList = new ArrayList<>();
        this.cmCraneMaintainPlanList = new ArrayList<>();
        this.cmCraneAddOrDeleteList = new ArrayList<>();
        this.cmCraneMoveRangeMap = new HashMap<>();
        this.workMoveMap = new LinkedHashMap<>();
        this.workBlockMap = new LinkedHashMap<>();
        this.craneEfficiencyMap = new LinkedHashMap<>();
        this.areaTaskMap = new LinkedHashMap<>();
        throughContainerMap = new HashMap<>();
        reStowContainerMapD = new HashMap<>();
        reStowContainerMapL = new HashMap<>();
    }

    public List<VMContainer> getRecycleCntList() {
        return recycleCntList;
    }

    public List<VMContainer> getSentContainerList() {
        return sentContainerList;
    }

    public List<VMContainer> getContinueCntList() {
        return continueCntList;
    }

    public CMCraneManual getCmCraneManual() {
        return cmCraneManual;
    }

    public void setCmCraneManual(CMCraneManual cmCraneManual) {
        this.cmCraneManual = cmCraneManual;
    }

    public Map<String, VMContainer> getDiscContainerMap() {
        return discContainerMap;
    }

    public Map<String, VMContainer> getLoadContainerMap() {
        return loadContainerMap;
    }

    public Map<String, List<WorkMove>> getWorkMoveMap() {
        return workMoveMap;
    }

    public void setWorkMoveMap(Map<String, List<WorkMove>> workMoveMap) {
        this.workMoveMap = workMoveMap;
    }

    public Map<String, List<WorkBlock>> getWorkBlockMap() {
        return workBlockMap;
    }

    public void setWorkBlockMap(Map<String, List<WorkBlock>> workBlockMap) {
        this.workBlockMap = workBlockMap;
    }

    public VMSchedule getVmSchedule() {
        return vmSchedule;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public CwpConfig getCwpConfig() {
        return cwpConfig;
    }

    public void setCwpConfig(CwpConfig cwpConfig) {
        this.cwpConfig = cwpConfig;
    }

    public String getCwpType() {
        return cwpType;
    }

    public void setCwpType(String cwpType) {
        this.cwpType = cwpType;
    }

    public void addCMCraneWorkFlow(CMCraneWorkFlow cmCraneWorkFlow) {
        cmCraneWorkFlowMap.put(cmCraneWorkFlow.getKey(), cmCraneWorkFlow);
    }

    public CMCraneWorkFlow getCMCraneWorkFlowByKey(String key) {
        return cmCraneWorkFlowMap.get(key);
    }

    public void putVMContainer(VMPosition vmPosition, VMContainer vmContainer) {
        Integer bayNo = vmPosition.getBayNo();
        Integer tierNo = vmPosition.getTierNo();
        Integer rowNo = vmPosition.getRowNo();
        if (bayNo % 2 == 0) { //大倍位
            if (vmContainer.getDlType().equals(CWPDomain.DL_TYPE_DISC)) { //卸船
                discContainerMap.put(new VMPosition(bayNo - 1, rowNo, tierNo).getVLocation(), vmContainer);
                discContainerMap.put(new VMPosition(bayNo + 1, rowNo, tierNo).getVLocation(), vmContainer);
            }
            if (vmContainer.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) { //装船
                loadContainerMap.put(new VMPosition(bayNo - 1, rowNo, tierNo).getVLocation(), vmContainer);
                loadContainerMap.put(new VMPosition(bayNo + 1, rowNo, tierNo).getVLocation(), vmContainer);
            }
        } else {
            if (vmContainer.getDlType().equals(CWPDomain.DL_TYPE_DISC)) { //卸船
                discContainerMap.put(vmPosition.getVLocation(), vmContainer);
            }
            if (vmContainer.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) { //装船
                loadContainerMap.put(vmPosition.getVLocation(), vmContainer);
            }
        }
    }

    public VMContainer getVMContainerByVMSlot(VMSlot vmSlot, String dlType) {
        if (vmSlot != null && dlType.equals(CWPDomain.DL_TYPE_DISC)) {
            return discContainerMap.get(vmSlot.getVmPosition().getVLocation());
        }
        if (vmSlot != null && dlType.equals(CWPDomain.DL_TYPE_LOAD)) {
            return loadContainerMap.get(vmSlot.getVmPosition().getVLocation());
        }
        return null;
    }

    public void putThroughVMContainer(VMPosition vmPosition, VMContainer vmContainer) {
        Integer bayNo = vmPosition.getBayNo();
        Integer tierNo = vmPosition.getTierNo();
        Integer rowNo = vmPosition.getRowNo();
        if (bayNo % 2 == 0) { //大倍位
            throughContainerMap.put(new VMPosition(bayNo - 1, rowNo, tierNo).getVLocation(), vmContainer);
            throughContainerMap.put(new VMPosition(bayNo + 1, rowNo, tierNo).getVLocation(), vmContainer);
        } else {
            throughContainerMap.put(vmPosition.getVLocation(), vmContainer);
        }
    }

    public VMContainer getThroughVMContainerByVMSlot(VMSlot vmSlot) {
        if (vmSlot != null) {
            return throughContainerMap.get(vmSlot.getVmPosition().getVLocation());
        }
        return null;
    }

    public Map<String, VMContainer> getReStowContainerMapD() {
        return reStowContainerMapD;
    }

    public void setReStowContainerMapD(Map<String, VMContainer> reStowContainerMapD) {
        this.reStowContainerMapD = reStowContainerMapD;
    }

    public Map<String, VMContainer> getReStowContainerMapL() {
        return reStowContainerMapL;
    }

    public void setReStowContainerMapL(Map<String, VMContainer> reStowContainerMapL) {
        this.reStowContainerMapL = reStowContainerMapL;
    }

    public List<VMContainerAmount> getVmContainerAmountList() {
        return vmContainerAmountList;
    }

    public void setVmContainerAmountList(List<VMContainerAmount> vmContainerAmountList) {
        this.vmContainerAmountList = vmContainerAmountList;
    }

    public void addWorkMove(WorkMove workMove) {
        for (VMSlot vmSlot : workMove.getVmSlotSet()) {
            if (workMove.getDlType().equals(CWPDomain.DL_TYPE_DISC)) { //卸船
                discWorkMoveMap.put(vmSlot.getVmPosition().getVLocation(), workMove);
            }
            if (workMove.getDlType().equals(CWPDomain.DL_TYPE_LOAD)) { //装船
                loadWorkMoveMap.put(vmSlot.getVmPosition().getVLocation(), workMove);
            }
        }
    }

    public WorkMove getWorkMoveByVMSlot(VMSlot vmSlot, String dlType) {
        if (vmSlot != null) {
            if (dlType.equals(CWPDomain.DL_TYPE_DISC)) {
                return discWorkMoveMap.get(vmSlot.getVmPosition().getVLocation());
            }
            if (dlType.equals(CWPDomain.DL_TYPE_LOAD)) {
                return loadWorkMoveMap.get(vmSlot.getVmPosition().getVLocation());
            }
        }
        return null;
    }

    public List<WorkMove> getAllBelowDiscD4Q2MoveListByBayNo(Integer bayNo) {
        List<WorkMove> workMoveList = new ArrayList<>();
        for (WorkMove workMove : discWorkMoveMap.values()) {
            if (workMove.getBayNo().equals(bayNo) && workMove.getTierNo() < 49 && (CWPCraneDomain.CT_DUAL40.equals(workMove.getWorkFlow()) || CWPCraneDomain.CT_QUAD20.equals(workMove.getWorkFlow()))) {
                workMoveList.add(workMove);
            }
        }
        return workMoveList;
    }

    public List<WorkMove> getAllBelowDiscMoveListByBayNo(Integer bayNo) {
        List<WorkMove> workMoveList = new ArrayList<>();
        for (WorkMove workMove : discWorkMoveMap.values()) {
            if (workMove.getBayNo().equals(bayNo) && workMove.getTierNo() < 49) {
                workMoveList.add(workMove);
            }
        }
        return workMoveList;
    }

    public List<WorkMove> getAllDiscHcMoveListByBayNo(Integer bayNo) {
        List<WorkMove> workMoveList = new ArrayList<>();
        for (WorkMove workMove : discWorkMoveMap.values()) {
            if (workMove.getBayNo().equals(bayNo) && CWPDomain.MOVE_TYPE_HC.equals(workMove.getMoveType())) {
                workMoveList.add(workMove);
            }
        }
        return workMoveList;
    }

    public Map<String, WorkMove> getDiscWorkMoveMap() {
        return discWorkMoveMap;
    }

    public Map<String, WorkMove> getLoadWorkMoveMap() {
        return loadWorkMoveMap;
    }


    public void addHatchBlock(HatchBlock hatchBlock) {
        this.hatchBlockMap.put(hatchBlock.getHatchId(), hatchBlock);
    }

    public HatchBlock getHatchBlockByHatchId(Long hatchId) {
        return this.hatchBlockMap.get(hatchId);
    }

    /**
     * 船舶左靠，反向靠泊（奇数排靠近海侧）;船舶右靠，正向靠泊（偶数排靠近海侧），得到奇数排号开始，还是偶数排号
     *
     * @param seaOrLand 从海侧开始，还是从陆侧开始
     * @return ROW_SEQ_ODD_EVEN：奇数到偶数
     */
    public String getOddOrEvenBySeaOrLand(String seaOrLand) {
        String planBerthDirect = vmSchedule.getPlanBerthDirect();
        boolean sl = seaOrLand.equals(CWPDomain.ROW_SEQ_SEA_LAND);
        String oe = sl ? CWPDomain.ROW_SEQ_ODD_EVEN : CWPDomain.ROW_SEQ_EVEN_ODD;
        String eo = sl ? CWPDomain.ROW_SEQ_EVEN_ODD : CWPDomain.ROW_SEQ_ODD_EVEN;
        return planBerthDirect.equals(CWPDomain.VES_BER_DIRECT_L) ? oe : eo;
    }

    public Integer getHcSeqByWorkMove(Long hatchId, WorkMove workMove) {
        HatchBlock hatchBlock = this.getHatchBlockByHatchId(hatchId);
//        String board = workMove.getDlType().equals(CWPDomain.DL_TYPE_DISC) ? CWPDomain.BOARD_ABOVE : CWPDomain.BOARD_BELOW;
        String board = null;
        if (workMove.getDlType().equals(CWPDomain.DL_TYPE_DISC)) {
            board = CWPDomain.BOARD_ABOVE;
        } else {
            if (workMove.getTierNo() > 49) {
                board = CWPDomain.BOARD_ABOVE;
            } else {
                board = CWPDomain.BOARD_BELOW;
            }
        }
        Integer hcSeq = hatchBlock.getHcSeqByRowNo(workMove.getRowNo(), board);
        if (hcSeq == null) {
            hcSeq = hatchBlock.getHcSeqByOtherRowNo(workMove.getRowNo(), board);
        }
        return hcSeq;
    }

    public double getBayPosition(Double bayHatchPo) {
        if (vmSchedule.getPlanBerthDirect().equals(CWPDomain.VES_BER_DIRECT_L)) {
            return CalculateUtil.add(vmSchedule.getPlanStartPst(), bayHatchPo);
        } else {
            return CalculateUtil.sub(vmSchedule.getPlanEndPst(), bayHatchPo);
        }
    }

    public void addCMCranePool(CMCranePool cmCranePool) {
        cmCranePoolList.add(cmCranePool);
    }

    public List<CMCranePool> getAllCMCranePools() {
        return cmCranePoolList;
    }

    public List<CMCraneAddOrDelete> getCmCraneAddOrDeleteList() {
        return cmCraneAddOrDeleteList;
    }

    public List<CMCraneMaintainPlan> getCmCraneMaintainPlanList() {
        return cmCraneMaintainPlanList;
    }

    public Map<String, List<CraneEfficiency>> getCraneEfficiencyMap() {
        return craneEfficiencyMap;
    }

    public void setCraneEfficiencyMap(Map<String, List<CraneEfficiency>> craneEfficiencyMap) {
        this.craneEfficiencyMap = craneEfficiencyMap;
    }

    public Map<String, List<AreaTask>> getAreaTaskMap() {
        return areaTaskMap;
    }

    public void setAreaTaskMap(Map<String, List<AreaTask>> areaTaskMap) {
        this.areaTaskMap = areaTaskMap;
    }

    public Map<String, CMCraneMoveRange> getCmCraneMoveRangeMap() {
        return cmCraneMoveRangeMap;
    }

    public void setCmCraneMoveRangeMap(Map<String, CMCraneMoveRange> cmCraneMoveRangeMap) {
        this.cmCraneMoveRangeMap = cmCraneMoveRangeMap;
    }
}
